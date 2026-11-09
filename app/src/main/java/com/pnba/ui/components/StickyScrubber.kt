package com.pnba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun StickyScrubber(
    letters: List<Char> = remember { ('A'..'Z').toList() },
    availableLetters: Set<Char>? = null,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    if (letters.isEmpty()) return

    var columnHeight by remember { mutableIntStateOf(1) }
    var activeChar by remember { mutableStateOf<Char?>(null) }
    var touchY by remember { mutableFloatStateOf(0f) }

    fun updateSelection(y: Float) {
        touchY = y.coerceIn(0f, columnHeight.toFloat())
        val fraction = (y / columnHeight.coerceAtLeast(1)).coerceIn(0f, 0.999f)
        val index = (fraction * letters.size).toInt().coerceIn(0, letters.size - 1)
        val letter = letters[index]
        if (letter != activeChar) {
            activeChar = letter
            onLetterSelected(letter)
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        val totalHeight = maxHeight
        val letterCount = letters.size
        val itemHeight = if (letterCount > 0) totalHeight / letterCount else totalHeight

        // Dynamically scale font size so 26 letters always fit smoothly in portrait & landscape
        val fontSize = when {
            itemHeight < 9.dp -> 7.sp
            itemHeight < 12.dp -> 8.sp
            itemHeight < 16.dp -> 9.sp
            else -> 10.sp
        }

        Box(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height
                }
                .pointerInput(letters, columnHeight) {
                    detectTapGestures(
                        onPress = { offset ->
                            updateSelection(offset.y)
                            tryAwaitRelease()
                            activeChar = null
                        }
                    )
                }
                .pointerInput(letters, columnHeight) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            updateSelection(offset.y)
                        },
                        onDragEnd = { activeChar = null },
                        onDragCancel = { activeChar = null },
                        onDrag = { change, _ ->
                            change.consume()
                            updateSelection(change.position.y)
                        }
                    )
                }
                .testTag("alphabetical_scrubber"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (char in letters) {
                    val isSelected = activeChar == char
                    val isAvailable = availableLetters == null || availableLetters.contains(char)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = fontSize,
                            lineHeight = fontSize,
                            fontWeight = if (isSelected) FontWeight.Black else if (isAvailable) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Floating Callout / Bubble showing the currently scrubbed letter beside the finger
        if (activeChar != null) {
            val density = LocalDensity.current
            val bubbleOffsetY = with(density) {
                (touchY - 24.dp.toPx()).roundToInt().coerceIn(
                    0,
                    (columnHeight - 48.dp.toPx()).roundToInt().coerceAtLeast(0)
                )
            }

            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = -with(density) { 34.dp.roundToPx() },
                                y = bubbleOffsetY
                            )
                        }
                        .size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = activeChar.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
