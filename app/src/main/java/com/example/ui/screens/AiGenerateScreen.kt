package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ai.AiMusicService
import com.example.model.AiPromptConfig
import com.example.model.DeckId
import com.example.ui.components.WaveformView
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
fun AiGenerateScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val aiConfig by viewModel.aiConfig.collectAsState()
    val genState by viewModel.aiGenState.collectAsState()

    val promptSuggestions = listOf(
        "Rolling Peak-Time Tech House 126 BPM",
        "Deep Hypnotic Cyber Techno with raw kick",
        "80s Retro Neon Synthwave with arpeggios",
        "Chilled Lo-Fi Boom-Bap Groove",
        "Energetic Acid Overdrive with screaming resonance"
    )

    val genres = listOf("Tech House", "Techno", "Synthwave", "Deep House", "Hip-Hop", "Ambient")
    val moods = listOf("Energetic", "Dark", "Chill", "Uplifting", "Hypnotic")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DjBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text("AI SONG GENERATOR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DjTextPrimary)
                    Text("Compose custom DJ stems, basslines & tracks via AI", fontSize = 11.sp, color = DjTextSecondary)
                }
            }
        }

        // Text Prompt Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("PROMPT / VIBE DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DjTextSecondary)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = aiConfig.prompt,
                        onValueChange = { viewModel.updateAiConfig(aiConfig.copy(prompt = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("ai_prompt_input"),
                        placeholder = { Text("e.g. Rolling warehouse techno with heavy kick and sub-bass...", fontSize = 12.sp, color = DjTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = DjSurfaceBorder,
                            focusedContainerColor = Color(0xFF10121A),
                            unfocusedContainerColor = Color(0xFF10121A),
                            focusedTextColor = DjTextPrimary,
                            unfocusedTextColor = DjTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("QUICK INSPIRATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DjTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(promptSuggestions) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E2230))
                                    .clickable {
                                        val derivedGenre = when {
                                            prompt.contains("Techno", true) -> "Techno"
                                            prompt.contains("Synthwave", true) -> "Synthwave"
                                            prompt.contains("Boom-Bap", true) -> "Hip-Hop"
                                            else -> "Tech House"
                                        }
                                        viewModel.updateAiConfig(aiConfig.copy(prompt = prompt, genre = derivedGenre))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(prompt, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }
        }

        // Genre & Mood Selectors
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("GENRE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DjTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genres) { g ->
                            FilterChip(
                                selected = aiConfig.genre == g,
                                onClick = { viewModel.updateAiConfig(aiConfig.copy(genre = g)) },
                                label = { Text(g, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA855F7).copy(alpha = 0.3f),
                                    selectedLabelColor = Color(0xFFA855F7)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("MOOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DjTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(moods) { m ->
                            FilterChip(
                                selected = aiConfig.mood == m,
                                onClick = { viewModel.updateAiConfig(aiConfig.copy(mood = m)) },
                                label = { Text(m, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeckACyan.copy(alpha = 0.3f),
                                    selectedLabelColor = DeckACyan
                                )
                            )
                        }
                    }
                }
            }
        }

        // BPM & Duration Sliders
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
                            Icon(Icons.Default.Speed, contentDescription = null, tint = DeckACyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEMPO: ${aiConfig.bpm} BPM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DjTextPrimary)
                        }
                        Text(if (aiConfig.bpm > 130) "Fast/Energy" else "Groove", fontSize = 11.sp, color = DjTextMuted)
                    }

                    Slider(
                        value = aiConfig.bpm.toFloat(),
                        onValueChange = { viewModel.updateAiConfig(aiConfig.copy(bpm = it.toInt())) },
                        valueRange = 80f..160f,
                        steps = 80,
                        colors = SliderDefaults.colors(
                            thumbColor = DeckACyan,
                            activeTrackColor = DeckACyan,
                            inactiveTrackColor = DjSurfaceBorder
                        ),
                        modifier = Modifier.testTag("ai_bpm_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = DeckBAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DURATION: ${aiConfig.durationSeconds}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DjTextPrimary)
                        }
                        Text("${aiConfig.durationSeconds / 4} Bars", fontSize = 11.sp, color = DjTextMuted)
                    }

                    Slider(
                        value = aiConfig.durationSeconds.toFloat(),
                        onValueChange = { viewModel.updateAiConfig(aiConfig.copy(durationSeconds = it.toInt())) },
                        valueRange = 15f..45f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = DeckBAmber,
                            activeTrackColor = DeckBAmber,
                            inactiveTrackColor = DjSurfaceBorder
                        ),
                        modifier = Modifier.testTag("ai_duration_slider")
                    )
                }
            }
        }

        // Generate Action Button
        item {
            val isGenerating = genState is AiMusicService.GenerationState.Generating

            Button(
                onClick = { viewModel.generateAiSong() },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_ai_track_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    disabledContainerColor = Color(0xFF4C1D95)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGenerating) "GENERATING TRACK..." else "GENERATE AI TRACK",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Live Generation Progress Indicator
        if (genState is AiMusicService.GenerationState.Generating) {
            val gen = genState as AiMusicService.GenerationState.Generating
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1736)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(gen.stepMessage, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { gen.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFA855F7),
                            trackColor = Color(0xFF332050)
                        )
                    }
                }
            }
        }

        // Generation Success Result Box
        if (genState is AiMusicService.GenerationState.Success) {
            val track = (genState as AiMusicService.GenerationState.Success).track
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF12241C)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DjGreenActive),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DjGreenActive, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Track Generated Successfully!", color = DjGreenActive, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(track.title, color = DjTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${track.genre} • ${track.bpm} BPM • Key: ${track.key} • Length: ${track.durationMs / 1000}s", color = DjTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(8.dp))

                        WaveformView(
                            deckId = DeckId.DECK_A,
                            waveform = track.waveform,
                            currentPositionMs = 0L,
                            durationMs = track.durationMs,
                            cuePointMs = 0L,
                            loopStartMs = 0L,
                            loopEndMs = 0L,
                            isLoopActive = false,
                            onSeek = {},
                            height = 36.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.loadTrackToDeck(DeckId.DECK_A, track)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("load_generated_deck_a"),
                                colors = ButtonDefaults.buttonColors(containerColor = DeckACyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("LOAD TO DECK A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.loadTrackToDeck(DeckId.DECK_B, track)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("load_generated_deck_b"),
                                colors = ButtonDefaults.buttonColors(containerColor = DeckBAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("LOAD TO DECK B", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Error Box
        if (genState is AiMusicService.GenerationState.Error) {
            val err = genState as AiMusicService.GenerationState.Error
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1216)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DjRedCue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DjRedCue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(err.message, color = DjRedCue, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
