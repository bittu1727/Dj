package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryKnob(
    label: String,
    value: Float, // -1.0f to 1.0f (representing -12dB to +12dB)
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "rotary_knob_${label.lowercase()}",
    size: Dp = 48.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DjTextSecondary,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(size)
                .testTag(testTag)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag up increases value, drag down decreases
                        val delta = -dragAmount.y * 0.015f
                        val updated = (value + delta).coerceIn(-1.0f, 1.0f)
                        onValueChange(updated)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f
                val trackRadius = radius - 4.dp.toPx()

                // Arc angles: 135° (bottom left) to 405° (bottom right) = 270° span
                val startAngle = 135f
                val sweepAngle = 270f
                val zeroAngle = 270f // Top dead center (0 dB)

                // Background track arc
                drawArc(
                    color = DjSurfaceBorder,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                    size = Size(trackRadius * 2f, trackRadius * 2f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active colored arc from center zero
                val activeSweep = (value * (sweepAngle / 2f))
                if (kotlin.math.abs(activeSweep) > 1f) {
                    drawArc(
                        color = accentColor,
                        startAngle = zeroAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                        size = Size(trackRadius * 2f, trackRadius * 2f),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Knob physical body
                val knobRadius = trackRadius - 4.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2E3444), Color(0xFF161922)),
                        center = center,
                        radius = knobRadius
                    ),
                    radius = knobRadius,
                    center = center
                )

                // Knob indicator notch
                val currentAngleDeg = zeroAngle + (value * (sweepAngle / 2f))
                val currentAngleRad = Math.toRadians(currentAngleDeg.toDouble())
                val notchInner = knobRadius * 0.35f
                val notchOuter = knobRadius * 0.88f

                val startP = Offset(
                    (center.x + notchInner * cos(currentAngleRad)).toFloat(),
                    (center.y + notchInner * sin(currentAngleRad)).toFloat()
                )
                val endP = Offset(
                    (center.x + notchOuter * cos(currentAngleRad)).toFloat(),
                    (center.y + notchOuter * sin(currentAngleRad)).toFloat()
                )

                drawLine(
                    color = if (kotlin.math.abs(value) < 0.05f) Color.White else accentColor,
                    start = startP,
                    end = endP,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // dB value readout
        val db = (value * 12f)
        val sign = if (db > 0.1f) "+" else ""
        Text(
            text = String.format(Locale.US, "%s%.1fdB", sign, db),
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            color = if (kotlin.math.abs(value) < 0.05f) DjTextMuted else accentColor
        )
    }
}
