package com.pnba.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pnba.R
import com.pnba.data.model.ReadingTheme
import com.pnba.data.network.AppUpdateInfo
import com.pnba.data.network.AppUpdateManager
import com.pnba.ui.HymnalViewModel
import com.pnba.ui.theme.SanctuaryHymnalTheme

@Composable
fun AboutScreen(
    viewModel: HymnalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showFeedbackDialog by viewModel.showFeedbackDialog.collectAsState()
    val isSubmittingFeedback by viewModel.isSubmittingFeedback.collectAsState()
    val feedbackStatus by viewModel.feedbackStatusMessage.collectAsState()
    val feedbackError by viewModel.feedbackErrorMessage.collectAsState()

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val updateCheckStatus by viewModel.updateCheckStatus.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStatus by viewModel.downloadStatusMessage.collectAsState()

    AboutScreenContent(
        showFeedbackDialog = showFeedbackDialog,
        isSubmitting = isSubmittingFeedback,
        feedbackStatus = feedbackStatus,
        feedbackError = feedbackError,
        updateInfo = updateInfo,
        isCheckingUpdate = isCheckingUpdate,
        updateCheckStatus = updateCheckStatus,
        isDownloadingUpdate = isDownloadingUpdate,
        downloadProgress = downloadProgress,
        downloadStatus = downloadStatus,
        onDismissFeedbackDialog = { viewModel.setFeedbackDialogVisible(false) },
        onOpenFeedbackDialog = { viewModel.setFeedbackDialogVisible(true) },
        onCheckUpdate = { viewModel.checkForAppUpdates(context) },
        onDownloadAndInstall = { url -> viewModel.downloadAndInstallUpdate(context, url) },
        onSubmitFeedback = { hymnNumber, detail, name, email ->
            viewModel.submitFeedback(
                hymnNumber = hymnNumber,
                typoDetail = detail,
                senderName = name,
                senderEmail = email,
                onFallbackEmail = {
                    sendFeedbackEmail(context, hymnNumber, detail, name, email)
                }
            )
        },
        onSendFallbackEmail = { hymnNumber, detail, name, email ->
            sendFeedbackEmail(context, hymnNumber, detail, name, email)
        },
        modifier = modifier
    )
}

@Composable
fun AboutScreenContent(
    showFeedbackDialog: Boolean,
    isSubmitting: Boolean,
    feedbackStatus: String?,
    feedbackError: String?,
    updateInfo: AppUpdateInfo?,
    isCheckingUpdate: Boolean,
    updateCheckStatus: String?,
    isDownloadingUpdate: Boolean = false,
    downloadProgress: Float? = null,
    downloadStatus: String? = null,
    onDismissFeedbackDialog: () -> Unit,
    onOpenFeedbackDialog: () -> Unit,
    onCheckUpdate: () -> Unit,
    onDownloadAndInstall: (String) -> Unit = {},
    onSubmitFeedback: (hymnNumber: String, detail: String, name: String, email: String) -> Unit,
    onSendFallbackEmail: (hymnNumber: String, detail: String, name: String, email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reportHymnNumber by remember { mutableStateOf("") }
    var reportDetail by remember { mutableStateOf("") }
    var reportName by remember { mutableStateOf("") }
    var reportEmail by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
    ) {
        // App Title & Emblem
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.pnba_logo),
                            contentDescription = "PNBA Logo",
                            modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Chipao Avei Loubvü",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Poumai Naga Baptist Association (PNBA) • Jesus is Lord",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Section 1: History of the Hymnal Collection
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HISTORY OF THIS COLLECTION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The Hymnal contains 520 hymns from the Chipao Avei Loubvü (Christian Hymn Book). It preserves the sacred heritage of congregational worship with Poula lyrics across all stanzas and refrains, paired with historic Solfa key signatures (Doh is ...), time signatures, classical hymn book references (S.S., C.W.H., Alex., Red.), English cross-reference hymn titles, authors, composers, and scriptural foundations.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Section 2: Copyright & Publication
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COPYRIGHT & PUBLICATION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Poumai Naga Baptist Association Christian Centre, Purul, P.O Maram Senapati District, Manipur, India.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Churches, choirs, fellowship groups, and home worshipers are freely encouraged for any modification, additions or corrections",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Section 3: Report Typo / Suggestion
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EDITORIAL REFINEMENT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Noticed an incorrect word, punctuation mark, or missing verse in your favorite hymn? Help us maintain complete liturgical accuracy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenFeedbackDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("report_typo_btn")
                    ) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Report a Typo or Request Hymn")
                    }
                }
            }
        }

        // Section 3.5: App Version & Updates
        item {
            val currentVersionName = remember(context) { AppUpdateManager.getCurrentVersionName(context) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "APP VERSION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Version $currentVersionName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Compact, smaller Check Updates button
                        OutlinedButton(
                            onClick = onCheckUpdate,
                            enabled = !isCheckingUpdate && !isDownloadingUpdate,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Checking...",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Check Updates",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    if (updateCheckStatus != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = updateCheckStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (updateInfo?.isUpdateAvailable == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Only show download/install UI when an update is actually available
                    if (updateInfo?.isUpdateAvailable == true && updateInfo.downloadUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (isDownloadingUpdate) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Downloading v${updateInfo.latestVersion}...",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = downloadStatus ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val prog = downloadProgress
                                    if (prog != null && prog > 0f) {
                                        LinearProgressIndicator(
                                            progress = { prog },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { onDownloadAndInstall(updateInfo.downloadUrl) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download & Install Release (v${updateInfo.latestVersion})")
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Developed By Credits & Studio Signature
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CHIPAO AVEI LOUBVÜ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "POUMAI NAGA BAPTIST ASSOCIATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "DESIGNED & DEVELOPED BY",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Oni Lana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    // Typo / Suggestion Dialog
    if (showFeedbackDialog) {
        Dialog(onDismissRequest = onDismissFeedbackDialog) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("feedback_modal_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Report Typo / Request Hymn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismissFeedbackDialog) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (feedbackStatus != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedbackStatus ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismissFeedbackDialog,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    } else {
                        Text(
                            text = "Submit corrections or missing verses for the hymnal:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = reportHymnNumber,
                            onValueChange = { reportHymnNumber = it },
                            label = { Text("Hymn Number (e.g. 2)") },
                            singleLine = true,
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = reportDetail,
                            onValueChange = { reportDetail = it },
                            label = { Text("Typo Details or Hymn Title *") },
                            placeholder = { Text("e.g. Verse 3 line 2 should read...") },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = reportName,
                            onValueChange = { reportName = it },
                            label = { Text("Your Name (optional)") },
                            placeholder = { Text("e.g. John Pou") },
                            singleLine = true,
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = reportEmail,
                            onValueChange = { reportEmail = it },
                            label = { Text("Your Email (optional)") },
                            placeholder = { Text("e.g. yourname@example.com") },
                            singleLine = true,
                            enabled = !isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (feedbackError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = feedbackError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            onSendFallbackEmail(reportHymnNumber, reportDetail, reportName, reportEmail)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Send via Email App Instead")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismissFeedbackDialog,
                                enabled = !isSubmitting
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSubmitFeedback(reportHymnNumber, reportDetail, reportName, reportEmail)
                                },
                                enabled = reportDetail.isNotBlank() && !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sending...")
                                } else {
                                    Text("Submit Feedback")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "About Screen - Light Parchment", showBackground = true)
@Composable
fun AboutScreenPreview() {
    SanctuaryHymnalTheme(readingTheme = ReadingTheme.PARCHMENT) {
        AboutScreenContent(
            showFeedbackDialog = false,
            isSubmitting = false,
            feedbackStatus = null,
            feedbackError = null,
            updateInfo = null,
            isCheckingUpdate = false,
            updateCheckStatus = null,
            onDismissFeedbackDialog = {},
            onOpenFeedbackDialog = {},
            onCheckUpdate = {},
            onSubmitFeedback = { _, _, _, _ -> },
            onSendFallbackEmail = { _, _, _, _ -> }
        )
    }
}

@Preview(name = "About Screen - Midnight Navy (Dark)", showBackground = true)
@Composable
fun AboutScreenDarkPreview() {
    SanctuaryHymnalTheme(readingTheme = ReadingTheme.NAVY) {
        AboutScreenContent(
            showFeedbackDialog = false,
            isSubmitting = false,
            feedbackStatus = null,
            feedbackError = null,
            updateInfo = null,
            isCheckingUpdate = false,
            updateCheckStatus = null,
            onDismissFeedbackDialog = {},
            onOpenFeedbackDialog = {},
            onCheckUpdate = {},
            onSubmitFeedback = { _, _, _, _ -> },
            onSendFallbackEmail = { _, _, _, _ -> }
        )
    }
}

@Preview(name = "About Screen - Feedback Dialog", showBackground = true)
@Composable
fun AboutScreenFeedbackDialogPreview() {
    SanctuaryHymnalTheme(readingTheme = ReadingTheme.PARCHMENT) {
        AboutScreenContent(
            showFeedbackDialog = true,
            isSubmitting = false,
            feedbackStatus = null,
            feedbackError = null,
            updateInfo = null,
            isCheckingUpdate = false,
            updateCheckStatus = null,
            onDismissFeedbackDialog = {},
            onOpenFeedbackDialog = {},
            onCheckUpdate = {},
            onSubmitFeedback = { _, _, _, _ -> },
            onSendFallbackEmail = { _, _, _, _ -> }
        )
    }
}

private fun sendFeedbackEmail(
    context: Context,
    hymnNumber: String,
    detail: String,
    senderName: String,
    senderEmail: String
) {
    val recipient = "onisimuslanamai@gmail.com"
    val subject = if (hymnNumber.isNotBlank()) {
        "Chipao Avei Loubvü Feedback: Hymn #$hymnNumber"
    } else {
        "Chipao Avei Loubvü Feedback & Suggestion"
    }
    val body = buildString {
        if (hymnNumber.isNotBlank()) {
            append("Hymn Number: #").append(hymnNumber.trim()).append("\n\n")
        }
        if (detail.isNotBlank()) {
            append("Details / Correction:\n").append(detail.trim()).append("\n\n")
        }
        if (senderName.isNotBlank()) {
            append("Submitted by: ").append(senderName.trim()).append("\n")
        }
        if (senderEmail.isNotBlank()) {
            append("Sender Email: ").append(senderEmail.trim()).append("\n")
        }
        append("\n--\nSent from Chipao Avei Loubvü App")
    }

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$recipient")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send Email via..."))
    } catch (_: Exception) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(fallback, "Send Email via..."))
        } catch (_: Exception) {}
    }
}
