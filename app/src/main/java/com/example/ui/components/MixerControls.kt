package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjRedCue
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextSecondary
import com.example.ui.theme.DjYellowWarn
import java.util.Locale

@Composable
fun VerticalSlider(
    label: String,
    value: Float, // 0.0f to 1.0f (or custom range mapped)
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 110.dp,
    width: Dp = 32.dp,
    isCenterDetent: Boolean = false,
    valueText: String? = null,
    testTag: String = "slider_${label.lowercase()}"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DjTextSecondary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF10121A))
                .border(1.dp, DjSurfaceBorder, RoundedCornerShape(6.dp))
                .testTag(testTag)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag up increases value
                        val delta = -dragAmount.y / size.height.toFloat()
                        onValueChange((value + delta).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newValue = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val trackWidth = 4.dp.toPx()
                val trackX = (size.width - trackWidth) / 2f

                // Track groove
                drawRoundRect(
                    color = Color(0xFF1E2230),
                    topLeft = Offset(trackX, 8.dp.toPx()),
                    size = Size(trackWidth, size.height - 16.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Center detent notch
                if (isCenterDetent) {
                    val centerY = size.height / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(4.dp.toPx(), centerY),
                        end = Offset(size.width - 4.dp.toPx(), centerY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Active level fill
                val activeHeight = (size.height - 16.dp.toPx()) * value
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.45f),
                    topLeft = Offset(trackX, size.height - 8.dp.toPx() - activeHeight),
                    size = Size(trackWidth, activeHeight),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Fader Knob cap
                val knobHeight = 16.dp.toPx()
                val knobWidth = size.width - 6.dp.toPx()
                val knobY = (size.height - 12.dp.toPx() - (value * (size.height - 24.dp.toPx()))) - knobHeight / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF384054), Color(0xFF1B1E28))
                    ),
                    topLeft = Offset(3.dp.toPx(), knobY),
                    size = Size(knobWidth, knobHeight),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )

                // Center line on knob
                drawLine(
                    color = accentColor,
                    start = Offset(6.dp.toPx(), knobY + knobHeight / 2f),
                    end = Offset(knobWidth, knobY + knobHeight / 2f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = valueText ?: String.format(Locale.US, "%.0f%%", value * 100),
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            color = DjTextMuted
        )
    }
}

@Composable
fun VuMeter(
    level: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    width: Dp = 14.dp,
    segments: Int = 12
) {
    Column(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0C0E14))
            .border(1.dp, DjSurfaceBorder, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 2.5.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in (segments - 1) downTo 0) {
            val segmentThreshold = i.toFloat() / segments
            val isLit = level >= segmentThreshold

            val segmentColor = when {
                i >= segments - 2 -> DjRedCue
                i >= segments - 5 -> DjYellowWarn
                else -> DjGreenActive
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isLit) segmentColor else segmentColor.copy(alpha = 0.12f))
            )
        }
    }
}

@Composable
fun Crossfader(
    value: Float, // -1.0f (Deck A) to +1.0f (Deck B)
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onValueChange(-1.0f) },
                modifier = Modifier.testTag("crossfader_snap_a"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("◄ A", color = DeckACyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { onValueChange(0.0f) },
                modifier = Modifier.testTag("crossfader_snap_center"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("CTR", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }

            TextButton(
                onClick = { onValueChange(1.0f) },
                modifier = Modifier.testTag("crossfader_snap_b"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("B ►", color = DeckBAmber, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Horizontal crossfader track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF10131B))
                .border(1.dp, DjSurfaceBorder, RoundedCornerShape(6.dp))
                .testTag("crossfader_slider")
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = (dragAmount.x / size.width.toFloat()) * 2.0f
                        onValueChange((value + delta).coerceIn(-1.0f, 1.0f))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val newValue = (fraction * 2.0f) - 1.0f
                        onValueChange(newValue.coerceIn(-1.0f, 1.0f))
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val centerY = size.height / 2f
                val grooveHeight = 5.dp.toPx()

                // Groove
                drawRoundRect(
                    color = Color(0xFF1C202E),
                    topLeft = Offset(12.dp.toPx(), centerY - grooveHeight / 2f),
                    size = Size(size.width - 24.dp.toPx(), grooveHeight),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Center notch
                val centerX = size.width / 2f
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(centerX, 8.dp.toPx()),
                    end = Offset(centerX, size.height - 8.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Left (Cyan) and Right (Amber) active guides
                val handleX = ((value + 1.0f) / 2.0f) * (size.width - 44.dp.toPx()) + 22.dp.toPx()

                // Slider Knob Cap
                val knobWidth = 32.dp.toPx()
                val knobHeight = size.height - 10.dp.toPx()
                val knobLeft = handleX - knobWidth / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF454E66), Color(0xFF222736), Color(0xFF141722))
                    ),
                    topLeft = Offset(knobLeft, 5.dp.toPx()),
                    size = Size(knobWidth, knobHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Knob center indicator line
                val indicatorColor = when {
                    value < -0.1f -> DeckACyan
                    value > 0.1f -> DeckBAmber
                    else -> Color.White
                }

                drawLine(
                    color = indicatorColor,
                    start = Offset(handleX, 7.dp.toPx()),
                    end = Offset(handleX, size.height - 7.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }
    }
}
