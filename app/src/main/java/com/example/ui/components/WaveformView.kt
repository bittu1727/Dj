package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.DeckId
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjRedCue
import com.example.ui.theme.DjSurfaceBorder

@Composable
fun WaveformView(
    deckId: DeckId,
    waveform: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    cuePointMs: Long,
    loopStartMs: Long,
    loopEndMs: Long,
    isLoopActive: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp
) {
    val accentColor = if (deckId == DeckId.DECK_A) DeckACyan else DeckBAmber
    val testTagPrefix = if (deckId == DeckId.DECK_A) "deck_a" else "deck_b"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .testTag("${testTagPrefix}_waveform")
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    if (durationMs > 0) {
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (fraction * durationMs).toLong()
                        onSeek(targetMs)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            // Dark backdrop with subtle grid line
            drawRect(color = Color(0xFF0F1117))

            // Center horizontal axis line
            drawLine(
                color = DjSurfaceBorder,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 1.dp.toPx()
            )

            val safeDuration = if (durationMs > 0) durationMs.toFloat() else 1f
            val progressFraction = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
            val playheadX = progressFraction * canvasWidth

            // Active loop highlight region
            if (isLoopActive && loopEndMs > loopStartMs) {
                val startX = (loopStartMs.toFloat() / safeDuration).coerceIn(0f, 1f) * canvasWidth
                val endX = (loopEndMs.toFloat() / safeDuration).coerceIn(0f, 1f) * canvasWidth
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.22f),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, canvasHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            // Draw waveform bars
            val points = if (waveform.isNotEmpty()) waveform else List(80) { 0.25f }
            val barSpacing = canvasWidth / points.size
            val barWidth = (barSpacing * 0.72f).coerceAtLeast(1.5f)

            for (i in points.indices) {
                val x = i * barSpacing + barWidth / 2f
                val amplitude = points[i].coerceIn(0.06f, 1.0f)
                val barHalfHeight = (amplitude * (canvasHeight * 0.44f)).coerceAtLeast(2.dp.toPx())

                val isPlayed = x <= playheadX
                val barColor = if (isPlayed) {
                    accentColor
                } else {
                    accentColor.copy(alpha = 0.35f)
                }

                drawLine(
                    color = barColor,
                    start = Offset(x, centerY - barHalfHeight),
                    end = Offset(x, centerY + barHalfHeight),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }

            // Cue point indicator (Crimson triangle/flag)
            if (cuePointMs > 0) {
                val cueX = (cuePointMs.toFloat() / safeDuration).coerceIn(0f, 1f) * canvasWidth
                drawLine(
                    color = DjRedCue,
                    start = Offset(cueX, 0f),
                    end = Offset(cueX, canvasHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Playhead Cursor (Bright White/Neon)
            drawLine(
                color = Color.White,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, canvasHeight),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}
