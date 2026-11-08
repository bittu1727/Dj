package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjBackground
import kotlin.math.atan2

@Composable
fun JogWheel(
    deckId: DeckId,
    isPlaying: Boolean,
    rotationAngle: Float,
    bpm: Int,
    timeText: String,
    onScratchNudge: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val accentColor = if (deckId == DeckId.DECK_A) DeckACyan else DeckBAmber
    val testTagPrefix = if (deckId == DeckId.DECK_A) "deck_a" else "deck_b"

    var lastAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(size)
            .shadow(12.dp, CircleShape, spotColor = accentColor)
            .clip(CircleShape)
            .testTag("${testTagPrefix}_jog_wheel")
            .pointerInput(deckId) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        lastAngle = (atan2(offset.y - center.y, offset.x - center.x) * 180f / Math.PI).toFloat()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val currentAngle = (atan2(change.position.y - center.y, change.position.x - center.x) * 180f / Math.PI).toFloat()
                        var delta = currentAngle - lastAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f
                        lastAngle = currentAngle
                        onScratchNudge(delta * 0.005f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            // Outer aluminum bezel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2C3242), Color(0xFF141720), Color(0xFF0C0E14)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Neon illuminated outer ring
            drawCircle(
                color = accentColor.copy(alpha = if (isPlaying) 0.9f else 0.4f),
                radius = radius - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Vinyl disc body with concentric vinyl grooves
            val vinylRadius = radius - 10.dp.toPx()
            drawCircle(
                color = Color(0xFF111319),
                radius = vinylRadius,
                center = center
            )

            // Draw grooved texture lines
            for (i in 1..4) {
                drawCircle(
                    color = Color(0xFF1F2430),
                    radius = vinylRadius * (0.35f + i * 0.14f),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Rotating turntable marker line & strobe dots
            rotate(rotationAngle, pivot = center) {
                // Turntable cue marker notch
                drawLine(
                    color = accentColor,
                    start = Offset(center.x, center.y - vinylRadius),
                    end = Offset(center.x, center.y - vinylRadius + 16.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Opposite secondary marker
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(center.x, center.y + vinylRadius - 12.dp.toPx()),
                    end = Offset(center.x, center.y + vinylRadius),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center vinyl label hub
            val hubRadius = vinylRadius * 0.48f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF202636), Color(0xFF0F1117)),
                    center = center,
                    radius = hubRadius
                ),
                radius = hubRadius,
                center = center
            )

            drawCircle(
                color = accentColor.copy(alpha = 0.8f),
                radius = hubRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center display (BPM & Time)
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${bpm} BPM\n$timeText",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
        }
    }
}
