package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.CrossfaderCurve
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjBackground
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.viewmodel.DjViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val mixerState by viewModel.mixerState.collectAsState()
    val recordedMixes by viewModel.recordedMixes.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DjBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2436))
                        .border(1.dp, DjSurfaceBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = DeckACyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("MIXER SETTINGS & RECORDINGS", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DjTextPrimary)
                    Text("Audio curves, output levels and exported mix sessions", fontSize = 11.sp, color = DjTextSecondary)
                }
            }
        }

        // Crossfader Curve Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CROSSFADER CURVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DjTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when (mixerState.crossfaderCurve) {
                            CrossfaderCurve.SMOOTH -> "Equal-Power (recommended for smooth house and techno mixing)"
                            CrossfaderCurve.LINEAR -> "Linear (proportional gain blend)"
                            CrossfaderCurve.SHARP -> "Sharp Cut (instant full volume on each side, ideal for scratching)"
                        },
                        fontSize = 11.sp,
                        color = DjTextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CrossfaderCurve.values().forEach { curve ->
                            FilterChip(
                                selected = mixerState.crossfaderCurve == curve,
                                onClick = { viewModel.audioEngine.setCrossfaderCurve(curve) },
                                label = { Text(curve.name, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeckACyan.copy(alpha = 0.25f),
                                    selectedLabelColor = DeckACyan
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("curve_${curve.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Master Volume Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DjGreenActive, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MASTER OUTPUT GAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DjTextSecondary)
                        }
                        Text(String.format(Locale.US, "%.0f%%", mixerState.masterVolume * 100), fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                    }

                    Slider(
                        value = mixerState.masterVolume,
                        onValueChange = { viewModel.audioEngine.setMasterVolume(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = DjGreenActive,
                            activeTrackColor = DjGreenActive,
                            inactiveTrackColor = DjSurfaceBorder
                        ),
                        modifier = Modifier.testTag("master_volume_slider")
                    )
                }
            }
        }

        // Recorded Mixes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RECORDED MIXES (${recordedMixes.size})", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DjTextPrimary)
                OutlinedButton(
                    onClick = { viewModel.refreshRecordingsList() },
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
                ) {
                    Text("REFRESH", fontSize = 9.5.sp, color = DjTextSecondary)
                }
            }
        }

        if (recordedMixes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = DjTextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No recorded mix sessions yet", color = DjTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Tap the REC MIX button on the mixer to capture your set!", color = DjTextMuted, fontSize = 10.5.sp)
                    }
                }
            }
        } else {
            items(recordedMixes) { file ->
                RecordedMixItem(
                    file = file,
                    onShare = { viewModel.shareMix(file) }
                )
            }
        }

        // Audio Engine Tech Specs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121A)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DjTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AUDIO ENGINE SPECIFICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DjTextMuted)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("• Sample Rate: 44,100 Hz Stereo (16-bit PCM)", fontSize = 10.sp, color = DjTextSecondary)
                    Text("• Equalizer: 3-Band Parametric Filter DSP", fontSize = 10.sp, color = DjTextSecondary)
                    Text("• Pitch Engine: Android Native Low-Latency PlaybackParams", fontSize = 10.sp, color = DjTextSecondary)
                    Text("• Recording Pipeline: Direct PCM Stream to RIFF/WAVE", fontSize = 10.sp, color = DjTextSecondary)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun RecordedMixItem(
    file: File,
    onShare: () -> Unit
) {
    val dateText = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
    val sizeKb = file.length() / 1024

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(file.name, color = DjTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("$dateText • ${sizeKb} KB • WAV", color = DjTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = DeckACyan.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeckACyan),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = DeckACyan, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHARE", color = DeckACyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
