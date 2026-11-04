package com.pnba.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pnba.data.model.Hymn
import com.pnba.ui.HymnalViewModel
import com.pnba.ui.components.PitchPipeDialog
import com.pnba.ui.components.ReaderSettingsSheet

@Composable
fun HymnDetailScreen(
    viewModel: HymnalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hymn = viewModel.getSelectedHymn()
    val isFavorite = viewModel.favoriteIds.collectAsState().value.contains(hymn.id)
    val readingTheme by viewModel.readingTheme.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val useSerifFont by viewModel.useSerifFont.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val activePitch by viewModel.activePlayingPitch.collectAsState()
    val userNote = viewModel.getNote(hymn.id)

    var showPitchPipe by remember { mutableStateOf(false) }
    var showReaderSettings by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNoteText by remember { mutableStateOf(userNote) }

    // Intercept back button to return to previous screen
    BackHandler {
        viewModel.navigateBack()
    }

    // Swipe horizontal drag detection for tactile page turns
    // Helper: detect a horizontal swipe that is clearly horizontal (dx >> dy)
    // using awaitEachGesture so it works even when a LazyColumn is nested inside.
    fun Modifier.horizontalSwipe(
        onSwipeLeft: () -> Unit,
        onSwipeRight: () -> Unit,
        threshold: Float = 120f
    ) = this.pointerInput(hymn.id) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var dx = 0f
            var dy = 0f
            var swiped = false
            do {
                val event = awaitPointerEvent()
                val drag = event.changes.firstOrNull() ?: break
                dx += drag.position.x - drag.previousPosition.x
                dy += drag.position.y - drag.previousPosition.y
                // Only act if clearly horizontal (2:1 ratio) and past threshold
                if (!swiped && kotlin.math.abs(dx) > threshold && kotlin.math.abs(dx) > kotlin.math.abs(dy) * 2) {
                    swiped = true
                    if (dx < 0) onSwipeLeft() else onSwipeRight()
                }
            } while (event.changes.any { it.pressed })
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("hymn_detail_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Number badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "HYMN #${hymn.id}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Action Tools
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pitch Pipe Chime
                    IconButton(
                        onClick = { showPitchPipe = true },
                        modifier = Modifier.testTag("open_pitch_pipe_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Choir Pitch Pipe",
                            tint = if (isPlayingAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Reader Font & Theme
                    IconButton(
                        onClick = { showReaderSettings = true },
                        modifier = Modifier.testTag("open_reader_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Reader Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Personal / Choir Note
                    IconButton(
                        onClick = {
                            editingNoteText = viewModel.getNote(hymn.id)
                            showNoteDialog = true
                        },
                        modifier = Modifier.testTag("open_note_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Choir Notes",
                            tint = if (userNote.isNotEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Bookmark Star
                    IconButton(
                        onClick = { viewModel.toggleFavorite(hymn.id) },
                        modifier = Modifier.testTag("toggle_favorite_detail")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Animated Hymn Content with smooth page slide transitions
            AnimatedContent(
                targetState = hymn,
                transitionSpec = {
                    if (targetState.id > initialState.id) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalSwipe(
                        onSwipeLeft  = { viewModel.nextHymn() },
                        onSwipeRight = { viewModel.prevHymn() }
                    ),
                label = "hymn_page_transition"
            ) { targetHymn ->
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = targetHymn.title,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )

                            if (targetHymn.englishTitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "(${targetHymn.englishTitle})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = targetHymn.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Author / Tune / Key Details Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (targetHymn.author.isNotBlank()) "Author: ${targetHymn.author}" else "Traditional",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val refText = if (targetHymn.hymnBookRef.isNotBlank()) targetHymn.hymnBookRef else targetHymn.tune
                                        if (refText.isNotBlank()) {
                                            Text(
                                                text = "Ref: $refText",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val meterStr = if (targetHymn.timeSignature.isNotBlank()) "Time: ${targetHymn.timeSignature}" else "Meter: ${targetHymn.meter}"
                                        Text(
                                            text = meterStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val keyStr = if (targetHymn.keySignature.isNotBlank()) targetHymn.keySignature else "Key: ${targetHymn.keynoteName}"
                                        Text(
                                            text = keyStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    if (targetHymn.composer.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Composer: ${targetHymn.composer}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (targetHymn.scripture.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Scripture: ${targetHymn.scripture}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Personal Note banner if present
                            if (userNote.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Note: $userNote",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Verses & Refrain
                    items(targetHymn.verses) { verse ->
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Verse Number Indicator
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .align(Alignment.Top)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${verse.verseNumber}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Stanza Lines
                                Column(modifier = Modifier.weight(1f)) {
                                    val hasChoralParts = remember(verse) {
                                        verse.lines.any { it.matches(Regex("^(Unison|Parts|All|S\\.P|T|T\\.B|Bass|A\\./T|Alto|SP/Alto|T/B|SP|SP/B|Tennor):.*")) }
                                    }
                                    for (line in verse.lines) {
                                        if (hasChoralParts) {
                                            val match = Regex("^([A-Za-z0-9./ ]+?:)\\s*(.*)$").find(line)
                                            val partLabel = match?.groupValues?.get(1) ?: ""
                                            val lyricText = if (match != null) match.groupValues[2] else line
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = partLabel,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontSize = fontSizeSp.sp,
                                                        lineHeight = (fontSizeSp * 1.5).sp,
                                                        fontStyle = FontStyle.Italic,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.width(74.dp)
                                                )
                                                Text(
                                                    text = lyricText,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontSize = fontSizeSp.sp,
                                                        lineHeight = (fontSizeSp * 1.5).sp,
                                                        fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                                    ),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = line,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = fontSizeSp.sp,
                                                    lineHeight = (fontSizeSp * 1.5).sp,
                                                    fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                                ),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }

                            // Refrain / Chorus (if defined)
                            if (targetHymn.chorus != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 36.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "CHORUS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        for (line in targetHymn.chorus) {
                                            Text(
                                                text = line,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = fontSizeSp.sp,
                                                    lineHeight = (fontSizeSp * 1.5).sp,
                                                    fontStyle = FontStyle.Italic,
                                                    fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.SansSerif
                                                ),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // End Amen / Doxology mark
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A M E N",
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 4.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Pitch Pipe Dialog Modal
        if (showPitchPipe) {
            PitchPipeDialog(
                hymn = hymn,
                isPlaying = isPlayingAudio,
                activePitch = activePitch,
                onPlayKeynote = { viewModel.playHymnStartingPitch(hymn) },
                onPlayMelody = { viewModel.playHymnMelody(hymn) },
                onPlayCustomPitch = { name, freq -> viewModel.playCustomPitch(name, freq) },
                onStop = { viewModel.stopAudio() },
                onDismiss = { showPitchPipe = false }
            )
        }

        // Reader Settings Sheet Modal
        if (showReaderSettings) {
            ReaderSettingsSheet(
                currentTheme = readingTheme,
                fontSizeSp = fontSizeSp,
                useSerif = useSerifFont,
                keepScreenAwake = keepScreenAwake,
                onThemeChanged = { viewModel.setReadingTheme(it) },
                onFontSizeChanged = { viewModel.setFontSize(it) },
                onSerifChanged = { viewModel.setUseSerif(it) },
                onKeepScreenAwakeChanged = { viewModel.setKeepScreenAwake(it) },
                onDismiss = { showReaderSettings = false }
            )
        }

        // Personal Note Modal Dialog
        if (showNoteDialog) {
            Dialog(onDismissRequest = { showNoteDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("note_dialog")
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
                                text = "Choir & Personal Note",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showNoteDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Text(
                            text = "Hymn #${hymn.id} - ${hymn.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editingNoteText,
                            onValueChange = { editingNoteText = it },
                            placeholder = { Text("e.g. Choir starts on second verse, Soprano lead, Key of G") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("note_input_field")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editingNoteText = ""
                                    viewModel.saveNote(hymn.id, "")
                                    showNoteDialog = false
                                }
                            ) {
                                Text("Clear")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.saveNote(hymn.id, editingNoteText)
                                    showNoteDialog = false
                                },
                                modifier = Modifier.testTag("save_note_btn")
                            ) {
                                Text("Save Note")
                            }
                        }
                    }
                }
            }
        }
    }
}
