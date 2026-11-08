package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjRedCue
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import java.util.Locale

@Composable
fun DeckCard(
    deckState: DeckState,
    onTogglePlay: () -> Unit,
    onCue: () -> Unit,
    onSync: () -> Unit,
    onToggleLoop: () -> Unit,
    onSeek: (Long) -> Unit,
    onPitchChange: (Float) -> Unit,
    onNudgePitch: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onEqChange: (low: Float, mid: Float, high: Float) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenTrimmer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeckA = deckState.deckId == DeckId.DECK_A
    val accentColor = if (isDeckA) DeckACyan else DeckBAmber
    val testTagPrefix = if (isDeckA) "deck_a" else "deck_b"
    val deckLabel = if (isDeckA) "DECK A" else "DECK B"

    val currentSec = (deckState.currentPositionMs / 1000)
    val durationSec = (deckState.durationMs / 1000)
    val timeFormatted = String.format(Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60)
    val totalFormatted = String.format(Locale.US, "%02d:%02d", durationSec / 60, durationSec % 60)

    val currentBpm = ((deckState.track?.bpm ?: 128) * deckState.pitchRate).toInt()

    Card(
        modifier = modifier
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("${testTagPrefix}_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deck Header: Identifier, Track Title, Key, BPM, Load Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = deckLabel,
                            color = accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = deckState.track?.title ?: "No Track Loaded",
                            color = DjTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${deckState.track?.key ?: "Am"} • $timeFormatted / $totalFormatted",
                            color = DjTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onOpenTrimmer,
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("${testTagPrefix}_trim_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
                    ) {
                        Text("TRIM", color = DjTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onOpenLibrary,
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("${testTagPrefix}_load_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.25f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Album,
                            contentDescription = "Load Track",
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("LOAD", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Waveform scrubber
            WaveformView(
                deckId = deckState.deckId,
                waveform = deckState.track?.waveform ?: emptyList(),
                currentPositionMs = deckState.currentPositionMs,
                durationMs = deckState.durationMs,
                cuePointMs = deckState.cuePointMs,
                loopStartMs = deckState.loopStartMs,
                loopEndMs = deckState.loopEndMs,
                isLoopActive = deckState.isLoopActive,
                onSeek = onSeek,
                height = 36.dp
            )

            // Main interaction row: Jog Wheel, Sliders, and Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Jog Wheel
                JogWheel(
                    deckId = deckState.deckId,
                    isPlaying = deckState.isPlaying,
                    rotationAngle = deckState.jogAngleDegrees,
                    bpm = currentBpm,
                    timeText = timeFormatted,
                    onScratchNudge = onNudgePitch,
                    size = 110.dp
                )

                // 3-Band Equalizer Knobs Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RotaryKnob(
                        label = "HI",
                        value = deckState.eqHigh,
                        onValueChange = { onEqChange(deckState.eqLow, deckState.eqMid, it) },
                        accentColor = accentColor,
                        size = 32.dp,
                        testTag = "${testTagPrefix}_eq_high"
                    )
                    RotaryKnob(
                        label = "MID",
                        value = deckState.eqMid,
                        onValueChange = { onEqChange(deckState.eqLow, it, deckState.eqHigh) },
                        accentColor = accentColor,
                        size = 32.dp,
                        testTag = "${testTagPrefix}_eq_mid"
                    )
                    RotaryKnob(
                        label = "LOW",
                        value = deckState.eqLow,
                        onValueChange = { onEqChange(it, deckState.eqMid, deckState.eqHigh) },
                        accentColor = accentColor,
                        size = 32.dp,
                        testTag = "${testTagPrefix}_eq_low"
                    )
                }

                // Volume Fader & VU Meter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VuMeter(
                        level = deckState.vuLevel,
                        height = 95.dp,
                        width = 8.dp
                    )

                    VerticalSlider(
                        label = "VOL",
                        value = deckState.volume,
                        onValueChange = onVolumeChange,
                        accentColor = accentColor,
                        height = 95.dp,
                        width = 26.dp,
                        testTag = "${testTagPrefix}_vol_slider"
                    )
                }

                // Pitch / Tempo Slider (-20% to +20%)
                val pitchNormalized = ((deckState.pitchRate - 0.8f) / 0.4f).coerceIn(0f, 1f)
                val pitchPercent = ((deckState.pitchRate - 1.0f) * 100f)
                val pitchSign = if (pitchPercent >= 0) "+" else ""

                VerticalSlider(
                    label = "PITCH",
                    value = pitchNormalized,
                    onValueChange = { norm ->
                        val rate = 0.8f + norm * 0.4f
                        onPitchChange(rate)
                    },
                    accentColor = accentColor,
                    height = 95.dp,
                    width = 26.dp,
                    isCenterDetent = true,
                    valueText = String.format(Locale.US, "%s%.0f%%", pitchSign, pitchPercent),
                    testTag = "${testTagPrefix}_pitch_slider"
                )
            }

            // Transport Control Buttons: CUE, PLAY/PAUSE, SYNC, LOOP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // CUE button
                Button(
                    onClick = onCue,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("${testTagPrefix}_cue_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DjRedCue.copy(alpha = 0.25f),
                        contentColor = DjRedCue
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DjRedCue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("CUE", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }

                // PLAY / PAUSE button
                Button(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("${testTagPrefix}_play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (deckState.isPlaying) DjGreenActive else Color(0xFF1E2536),
                        contentColor = if (deckState.isPlaying) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (deckState.isPlaying) DjGreenActive else accentColor
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (deckState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (deckState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (deckState.isPlaying) "PLAY" else "PLAY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // SYNC button
                Button(
                    onClick = onSync,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("${testTagPrefix}_sync_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (deckState.isSynced) accentColor else Color(0xFF171B26),
                        contentColor = if (deckState.isSynced) Color.Black else accentColor
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("SYNC", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                // LOOP button
                Button(
                    onClick = onToggleLoop,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("${testTagPrefix}_loop_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (deckState.isLoopActive) Color(0xFF7C3AED) else Color(0xFF171B26),
                        contentColor = if (deckState.isLoopActive) Color.White else Color(0xFFA78BFA)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA78BFA)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = "Loop", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (deckState.isLoopActive) "LOOP" else "LOOP", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}
