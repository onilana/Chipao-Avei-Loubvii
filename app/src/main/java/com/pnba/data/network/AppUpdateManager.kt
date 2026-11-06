package com.pnba.data.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.pnba.util.AppNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val latestVersionCode: Int = 0,
    val releaseNotes: String,
    val downloadUrl: String
)

object AppUpdateManager {

    /**
     * Compares semantic version names (e.g. "1.3" vs "1.2", "1.4" vs "1.3").
     * Returns true ONLY if [latest] is strictly newer than [current].
     */
    fun isVersionNewer(latest: String, current: String): Boolean {
        val cleanLatest = latest.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")
        if (cleanLatest.isBlank() || cleanCurrent.isBlank()) return false
        if (cleanLatest.equals(cleanCurrent, ignoreCase = true)) return false

        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    suspend fun checkForUpdates(context: Context): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val currentName = getCurrentVersionName(context)
        val currentCode = getCurrentVersionCode(context)

        val defaultUrl = FeedbackConfig.UPDATE_CHECK_URL.ifBlank {
            if (FeedbackConfig.WEBHOOK_URL.isNotBlank()) {
                "${FeedbackConfig.WEBHOOK_URL}?action=check_update"
            } else ""
        }
        val checkUrl = RemoteConfigManager.getString(
            RemoteConfigManager.KEY_UPDATE_CHECK_URL,
            defaultUrl
        )

        if (checkUrl.isBlank()) {
            return@withContext Result.success(
                AppUpdateInfo(
                    isUpdateAvailable = false,
                    currentVersion = currentName,
                    latestVersion = currentName,
                    latestVersionCode = currentCode,
                    releaseNotes = "You are on the latest version.",
                    downloadUrl = ""
                )
            )
        }

        try {
            val url = URL(checkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "ChipaoAveiLoubvu-Android")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val jsonString = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                connection.disconnect()
                val json = JSONObject(jsonString)
                val latestCode = json.optInt("latestVersionCode", currentCode)
                val latestName = json.optString("latestVersionName", currentName)
                val notes = json.optString("releaseNotes", "")
                val downloadUrl = json.optString("downloadUrl", "")

                val updateAvailable = isVersionNewer(latest = latestName, current = currentName)
                if (updateAvailable) {
                    AppNotificationManager.showUpdateNotification(
                        context = context,
                        latestVersionName = latestName,
                        releaseNotes = notes,
                        downloadUrl = downloadUrl
                    )
                }

                Result.success(
                    AppUpdateInfo(
                        isUpdateAvailable = updateAvailable,
                        currentVersion = currentName,
                        latestVersion = latestName,
                        latestVersionCode = latestCode,
                        releaseNotes = notes,
                        downloadUrl = downloadUrl
                    )
                )
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP $responseCode from update server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK directly inside the app with redirect following and progress reporting.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = downloadUrl
            var connection: HttpURLConnection? = null
            var redirects = 0
            val maxRedirects = 6

            while (redirects < maxRedirects) {
                val u = URL(currentUrl)
                connection = (u.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "ChipaoAveiLoubvu-Android")
                    setRequestProperty("Accept", "*/*")
                }

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (!location.isNullOrBlank()) {
                        currentUrl = location
                        redirects++
                        continue
                    }
                }
                break
            }

            val conn = connection ?: throw IllegalStateException("Could not establish connection")
            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                conn.disconnect()
                return@withContext Result.failure(Exception("Server returned HTTP $responseCode"))
            }

            val totalBytes = conn.contentLength.toLong()
            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "chipao-update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            var downloadedBytes = 0L
            conn.inputStream.use { input: InputStream ->
                FileOutputStream(apkFile).use { output: FileOutputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = downloadedBytes.toFloat() / totalBytes.toFloat()
                            val mbDownloaded = downloadedBytes / (1024f * 1024f)
                            val mbTotal = totalBytes / (1024f * 1024f)
                            val status = String.format("%.1f MB / %.1f MB (%.0f%%)", mbDownloaded, mbTotal, progress * 100f)
                            onProgress(progress, status)
                        } else {
                            val mbDownloaded = downloadedBytes / (1024f * 1024f)
                            val status = String.format("%.1f MB downloaded", mbDownloaded)
                            onProgress(0.5f, status)
                        }
                    }
                    output.flush()
                }
            }
            conn.disconnect()

            Result.success(apkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Installs the downloaded APK via FileProvider without leaving the app for an external browser.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        // On Android 8.0+ (Oreo), check if app is permitted to request install packages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }
}
