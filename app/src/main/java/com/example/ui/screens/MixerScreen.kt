package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.MixerState
import com.example.ui.components.Crossfader
import com.example.ui.components.DeckCard
import com.example.ui.components.VerticalSlider
import com.example.ui.components.VuMeter
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
fun MixerScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val deckAState by viewModel.deckAState.collectAsState()
    val deckBState by viewModel.deckBState.collectAsState()
    val mixerState by viewModel.mixerState.collectAsState()
    val tapBpm by viewModel.tapBpm.collectAsState()

    // Full Horizontal Console Layout: Left Deck A | Center Mixer | Right Deck B
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DjBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Deck A (Cyan)
        DeckCard(
            deckState = deckAState,
            onTogglePlay = { viewModel.audioEngine.togglePlay(DeckId.DECK_A) },
            onCue = { viewModel.audioEngine.cueDeck(DeckId.DECK_A) },
            onSync = { viewModel.audioEngine.syncDecks(DeckId.DECK_B) },
            onToggleLoop = { viewModel.audioEngine.toggleLoop(DeckId.DECK_A) },
            onSeek = { viewModel.audioEngine.seekTo(DeckId.DECK_A, it) },
            onPitchChange = { viewModel.audioEngine.setPitchRate(DeckId.DECK_A, it) },
            onNudgePitch = { viewModel.audioEngine.nudgePitch(DeckId.DECK_A, it) },
            onVolumeChange = { viewModel.audioEngine.setDeckVolume(DeckId.DECK_A, it) },
            onEqChange = { low, mid, high -> viewModel.audioEngine.setEq(DeckId.DECK_A, low, mid, high) },
            onOpenLibrary = { viewModel.selectTab(DjViewModel.AppTab.LIBRARY) },
            onOpenTrimmer = {
                deckAState.track?.let { viewModel.openTrackInTrimmer(it, DeckId.DECK_A) }
            },
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight()
        )

        // Central Mixer Section
        CentralMixerColumn(
            mixerState = mixerState,
            deckAState = deckAState,
            deckBState = deckBState,
            tapBpm = tapBpm,
            onToggleRecording = { viewModel.toggleRecording() },
            onRegisterTap = { viewModel.registerTapTempo() },
            onTriggerAutoMix = { target -> viewModel.audioEngine.triggerAutoMix(target, 6) },
            onCrossfaderChange = { viewModel.audioEngine.setCrossfader(it) },
            onMasterVolumeChange = { viewModel.audioEngine.setMasterVolume(it) },
            modifier = Modifier
                .weight(0.72f)
                .fillMaxHeight()
        )

        // Deck B (Amber)
        DeckCard(
            deckState = deckBState,
            onTogglePlay = { viewModel.audioEngine.togglePlay(DeckId.DECK_B) },
            onCue = { viewModel.audioEngine.cueDeck(DeckId.DECK_B) },
            onSync = { viewModel.audioEngine.syncDecks(DeckId.DECK_A) },
            onToggleLoop = { viewModel.audioEngine.toggleLoop(DeckId.DECK_B) },
            onSeek = { viewModel.audioEngine.seekTo(DeckId.DECK_B, it) },
            onPitchChange = { viewModel.audioEngine.setPitchRate(DeckId.DECK_B, it) },
            onNudgePitch = { viewModel.audioEngine.nudgePitch(DeckId.DECK_B, it) },
            onVolumeChange = { viewModel.audioEngine.setDeckVolume(DeckId.DECK_B, it) },
            onEqChange = { low, mid, high -> viewModel.audioEngine.setEq(DeckId.DECK_B, low, mid, high) },
            onOpenLibrary = { viewModel.selectTab(DjViewModel.AppTab.LIBRARY) },
            onOpenTrimmer = {
                deckBState.track?.let { viewModel.openTrackInTrimmer(it, DeckId.DECK_B) }
            },
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun CentralMixerColumn(
    mixerState: MixerState,
    deckAState: DeckState,
    deckBState: DeckState,
    tapBpm: Int?,
    onToggleRecording: () -> Unit,
    onRegisterTap: () -> Unit,
    onTriggerAutoMix: (DeckId) -> Unit,
    onCrossfaderChange: (Float) -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse_h")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha_h"
    )

    Card(
        modifier = modifier
            .border(1.5.dp, DjSurfaceBorder, RoundedCornerShape(12.dp))
            .testTag("central_mixer_console"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Action Strip: REC, TAP, AUTO-MIX
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // REC button
                val recSec = mixerState.recordingDurationMs / 1000
                val recTime = String.format(Locale.US, "%02d:%02d", recSec / 60, recSec % 60)
                Button(
                    onClick = onToggleRecording,
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .testTag("master_rec_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mixerState.isRecording) DjRedCue.copy(alpha = recAlpha) else Color(0xFF222838),
                        contentColor = if (mixerState.isRecording) Color.White else DjRedCue
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(if (mixerState.isRecording) recTime else "REC", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // TAP BPM button
                Button(
                    onClick = onRegisterTap,
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .testTag("tap_tempo_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(11.dp), tint = DeckACyan)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (tapBpm != null) "$tapBpm" else "TAP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // AUTO-MIX button
                val nextDeck = if (mixerState.crossfader <= 0f) DeckId.DECK_B else DeckId.DECK_A
                Button(
                    onClick = { onTriggerAutoMix(nextDeck) },
                    enabled = !mixerState.isAutoMixActive,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(28.dp)
                        .testTag("auto_mix_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mixerState.isAutoMixActive) DjGreenActive else Color(0xFF232D3F),
                        contentColor = if (mixerState.isAutoMixActive) Color.Black else DjGreenActive
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (mixerState.isAutoMixActive) "MIX..." else "AUTO", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Auto-Mix progress indicator
            AnimatedVisibility(visible = mixerState.isAutoMixActive) {
                LinearProgressIndicator(
                    progress = { mixerState.autoMixProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = DjGreenActive,
                    trackColor = DjSurfaceBorder
                )
            }

            // Center: Dual Master VU Meters & Master Level
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deck A Level Meter (Cyan glow)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("A", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeckACyan)
                    Spacer(modifier = Modifier.height(2.dp))
                    VuMeter(level = deckAState.vuLevel, height = 80.dp, width = 8.dp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Master Volume Slider
                VerticalSlider(
                    label = "MASTER",
                    value = mixerState.masterVolume,
                    onValueChange = onMasterVolumeChange,
                    accentColor = DjGreenActive,
                    height = 80.dp,
                    width = 28.dp,
                    testTag = "master_volume_slider"
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Deck B Level Meter (Amber glow)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("B", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeckBAmber)
                    Spacer(modifier = Modifier.height(2.dp))
                    VuMeter(level = deckBState.vuLevel, height = 80.dp, width = 8.dp)
                }
            }

            // Bottom: Crossfader Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Crossfader(
                    value = mixerState.crossfader,
                    onValueChange = onCrossfaderChange
                )
            }
        }
    }
}
