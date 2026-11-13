package com.pnba.data.network

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.pnba.BuildConfig
import com.pnba.util.AppCrashReporter

/**
 * Model representing an active announcement broadcast from Firebase Remote Config.
 */
data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val actionUrl: String? = null
)

/**
 * Manages Firebase Remote Config parameters, fallbacks, and real-time/periodic fetches.
 */
object RemoteConfigManager {

    // Config Keys
    const val KEY_UPDATE_CHECK_URL = "update_check_url"
    const val KEY_DOWNLOAD_URL = "download_url"
    const val KEY_FORCE_UPDATE_REQUIRED = "force_update_required"
    const val KEY_MIN_VERSION_CODE = "min_version_code"
    const val KEY_ANNOUNCEMENT_ENABLED = "announcement_enabled"
    const val KEY_ANNOUNCEMENT_ID = "announcement_id"
    const val KEY_ANNOUNCEMENT_TITLE = "announcement_title"
    const val KEY_ANNOUNCEMENT_TEXT = "announcement_text"
    const val KEY_ANNOUNCEMENT_URL = "announcement_url"
    const val KEY_FEEDBACK_WEBHOOK_URL = "feedback_webhook_url"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    /**
     * Initialize Remote Config with in-app defaults and fetch the latest values.
     * Fallbacks ensure the app works seamlessly even without an internet connection.
     */
    fun initialize(onFetched: (() -> Unit)? = null) {
        try {
            // Set fetch interval: 0 in debug for instant testing, 1 hour in release
            val intervalSeconds = if (BuildConfig.DEBUG) 0L else 3600L
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(intervalSeconds)
                .build()

            remoteConfig.setConfigSettingsAsync(configSettings)

            // Define safe local defaults
            val defaults = mapOf<String, Any>(
                KEY_UPDATE_CHECK_URL to FeedbackConfig.UPDATE_CHECK_URL,
                KEY_DOWNLOAD_URL to "https://github.com/onilana/Chipao-Avei-Loubvii/releases/latest/download/app-release.apk",
                KEY_FORCE_UPDATE_REQUIRED to false,
                KEY_MIN_VERSION_CODE to 1L,
                KEY_ANNOUNCEMENT_ENABLED to false,
                KEY_ANNOUNCEMENT_ID to "",
                KEY_ANNOUNCEMENT_TITLE to "Announcement",
                KEY_ANNOUNCEMENT_TEXT to "",
                KEY_ANNOUNCEMENT_URL to "",
                KEY_FEEDBACK_WEBHOOK_URL to FeedbackConfig.WEBHOOK_URL
            )

            remoteConfig.setDefaultsAsync(defaults).addOnCompleteListener {
                remoteConfig.fetchAndActivate()
                    .addOnSuccessListener {
                        AppCrashReporter.log("RemoteConfig: Fetch and activate succeeded")
                        onFetched?.invoke()
                    }
                    .addOnFailureListener { error ->
                        AppCrashReporter.log("RemoteConfig: Fetch failed, using cached/defaults: ${error.message}")
                    }
            }
        } catch (e: Exception) {
            AppCrashReporter.recordException(e)
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return try {
            val value = remoteConfig.getString(key)
            if (value.isNotBlank()) value else defaultValue
        } catch (_: Exception) {
            defaultValue
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            remoteConfig.getBoolean(key)
        } catch (_: Exception) {
            defaultValue
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            remoteConfig.getLong(key)
        } catch (_: Exception) {
            defaultValue
        }
    }

    /**
     * Resolves the update check URL: prefers Remote Config override if present,
     * otherwise falls back to GitHub raw version.json.
     */
    fun getUpdateCheckUrl(): String {
        return getString(KEY_UPDATE_CHECK_URL, FeedbackConfig.UPDATE_CHECK_URL)
    }

    /**
     * Checks if minimum version code requires forced update.
     */
    fun isUpdateForced(currentVersionCode: Int): Boolean {
        val minCode = getLong(KEY_MIN_VERSION_CODE, 0L)
        val forceFlag = getBoolean(KEY_FORCE_UPDATE_REQUIRED, false)
        return forceFlag || (minCode > 0 && currentVersionCode < minCode)
    }

    /**
     * Retrieves the active announcement if enabled, non-empty, and has an ID.
     */
    fun getActiveAnnouncement(): Announcement? {
        val isEnabled = getBoolean(KEY_ANNOUNCEMENT_ENABLED, false)
        val text = getString(KEY_ANNOUNCEMENT_TEXT, "").trim()
        val id = getString(KEY_ANNOUNCEMENT_ID, "").trim()

        if (!isEnabled || text.isBlank() || id.isBlank()) return null

        val title = getString(KEY_ANNOUNCEMENT_TITLE, "Announcement").trim()
        val actionUrl = getString(KEY_ANNOUNCEMENT_URL, "").trim().takeIf { it.isNotBlank() }

        return Announcement(
            id = id,
            title = if (title.isNotBlank()) title else "Announcement",
            message = text,
            actionUrl = actionUrl
        )
    }
}
