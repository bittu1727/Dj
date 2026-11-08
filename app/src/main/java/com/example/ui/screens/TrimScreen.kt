package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjBackground
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjRedCue
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.viewmodel.DjViewModel
import java.util.Locale

@Composable
fun TrimScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val trimState by viewModel.trimState.collectAsState()
    val track = trimState.track

    var isPreviewing by remember { mutableStateOf(false) }
    var targetDeck by remember { mutableStateOf(trimState.targetDeck) }

    if (track == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DjBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No track selected for editing", color = DjTextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.LIBRARY) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeckACyan),
                    modifier = Modifier.testTag("trim_open_library_button")
                ) {
                    Text("SELECT TRACK FROM LIBRARY", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DjBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = DeckACyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SONG CUTTER & LOOP STUDIO", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DjTextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${track.title} (${track.bpm} BPM)", color = DjTextSecondary, fontSize = 11.sp)
                }

                // Target Deck Selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = targetDeck == DeckId.DECK_A,
                        onClick = { targetDeck = DeckId.DECK_A },
                        label = { Text("DECK A", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeckACyan.copy(alpha = 0.25f),
                            selectedLabelColor = DeckACyan
                        ),
                        modifier = Modifier.testTag("trim_target_deck_a")
                    )
                    FilterChip(
                        selected = targetDeck == DeckId.DECK_B,
                        onClick = { targetDeck = DeckId.DECK_B },
                        label = { Text("DECK B", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeckBAmber.copy(alpha = 0.25f),
                            selectedLabelColor = DeckBAmber
                        ),
                        modifier = Modifier.testTag("trim_target_deck_b")
                    )
                }
            }
        }

        // Waveform Cutter Canvas
        item {
            val durationMs = track.durationMs.coerceAtLeast(1L)
            val startFraction = (trimState.startMs.toFloat() / durationMs).coerceIn(0f, 1f)
            val endFraction = (trimState.endMs.toFloat() / durationMs).coerceIn(0f, 1f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .testTag("trim_waveform_canvas"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E14)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DjSurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(durationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val targetMs = (fraction * durationMs).toLong()

                                val distStart = kotlin.math.abs(targetMs - trimState.startMs)
                                val distEnd = kotlin.math.abs(targetMs - trimState.endMs)
                                if (distStart < distEnd) {
                                    viewModel.updateTrimPoints(targetMs, trimState.endMs)
                                } else {
                                    viewModel.updateTrimPoints(trimState.startMs, targetMs)
                                }
                            }
                        }
                        .pointerInput(durationMs) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val targetMs = (fraction * durationMs).toLong()
                                if (targetMs < trimState.startMs) {
                                    viewModel.updateTrimPoints(targetMs, trimState.endMs)
                                } else {
                                    viewModel.updateTrimPoints(trimState.startMs, targetMs)
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val centerY = h / 2f

                        val startX = startFraction * w
                        val endX = endFraction * w

                        // Highlight selected region
                        drawRoundRect(
                            color = DeckACyan.copy(alpha = 0.22f),
                            topLeft = Offset(startX, 0f),
                            size = Size(endX - startX, h),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Draw waveform bars
                        val points = if (track.waveform.isNotEmpty()) track.waveform else List(120) { 0.3f }
                        val barSpacing = w / points.size
                        val barWidth = (barSpacing * 0.75f).coerceAtLeast(1.5f)

                        for (i in points.indices) {
                            val x = i * barSpacing + barWidth / 2f
                            val amp = points[i].coerceIn(0.06f, 1.0f)
                            val barHalfH = (amp * (h * 0.42f)).coerceAtLeast(2.dp.toPx())

                            val isInsideTrim = x in startX..endX
                            val barColor = if (isInsideTrim) DeckACyan else DjTextMuted.copy(alpha = 0.35f)

                            drawLine(
                                color = barColor,
                                start = Offset(x, centerY - barHalfH),
                                end = Offset(x, centerY + barHalfH),
                                strokeWidth = barWidth,
                                cap = StrokeCap.Round
                            )
                        }

                        // Left Start Handle (Green flag)
                        drawLine(
                            color = DjGreenActive,
                            start = Offset(startX, 0f),
                            end = Offset(startX, h),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(color = DjGreenActive, radius = 5.dp.toPx(), center = Offset(startX, 7.dp.toPx()))

                        // Right End Handle (Red flag)
                        drawLine(
                            color = DjRedCue,
                            start = Offset(endX, 0f),
                            end = Offset(endX, h),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(color = DjRedCue, radius = 5.dp.toPx(), center = Offset(endX, h - 7.dp.toPx()))
                    }
                }
            }
        }

        // Time Markers and Controls Row
        item {
            val startSec = trimState.startMs / 1000f
            val endSec = trimState.endMs / 1000f
            val rangeSec = (trimState.endMs - trimState.startMs) / 1000f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = String.format(Locale.US, "IN: %.2fs", startSec),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DjGreenActive,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "LOOP: %.2fs", rangeSec),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DeckACyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "OUT: %.2fs", endSec),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DjRedCue,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val msPerBeat = 60000L / track.bpm
                    listOf(
                        "1 BAR" to msPerBeat * 4,
                        "2 BARS" to msPerBeat * 8,
                        "4 BARS" to msPerBeat * 16
                    ).forEach { (label, beatMs) ->
                        OutlinedButton(
                            onClick = {
                                val newEnd = (trimState.startMs + beatMs).coerceAtMost(track.durationMs)
                                viewModel.updateTrimPoints(trimState.startMs, newEnd)
                            },
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isPreviewing = !isPreviewing
                        if (isPreviewing) {
                            viewModel.audioEngine.seekTo(targetDeck, trimState.startMs)
                            if (!if (targetDeck == DeckId.DECK_A) viewModel.deckAState.value.isPlaying else viewModel.deckBState.value.isPlaying) {
                                viewModel.audioEngine.togglePlay(targetDeck)
                            }
                        } else {
                            if (if (targetDeck == DeckId.DECK_A) viewModel.deckAState.value.isPlaying else viewModel.deckBState.value.isPlaying) {
                                viewModel.audioEngine.togglePlay(targetDeck)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("trim_preview_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPreviewing) DjRedCue else Color(0xFF1E2433),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPreviewing) "STOP PREVIEW" else "PREVIEW LOOP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.applyTrimToDeck()
                        viewModel.selectTab(DjViewModel.AppTab.MIXER)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .testTag("trim_apply_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (targetDeck == DeckId.DECK_A) DeckACyan else DeckBAmber,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("APPLY TO ${targetDeck.name}", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (trimState.statusMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    trimState.statusMessage!!,
                    color = DjGreenActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
