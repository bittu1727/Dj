package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.Track
import com.example.ui.components.WaveformView
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjBackground
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjSurface
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.viewmodel.DjViewModel
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLibraryLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All/Bundled, 1 = AI Tracks, 2 = Local Files

    // File picker launcher for local audio files (zero permission overhead)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "Imported_Audio_${System.currentTimeMillis()}"
            viewModel.importLocalAudio(uri, fileName)
        }
    }

    val filteredTracks = tracks.filter { track ->
        val matchesSearch = track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.genre.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedTab) {
            1 -> track.isAiGenerated
            2 -> track.uri != null
            else -> true
        }

        matchesSearch && matchesTab
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjBackground)
            .padding(12.dp)
    ) {
        // Search & Import Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("library_search_input"),
                placeholder = { Text("Search title, artist, genre...", fontSize = 12.sp, color = DjTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DjTextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeckACyan,
                    unfocusedBorderColor = DjSurfaceBorder,
                    focusedContainerColor = DjSurfaceElevated,
                    unfocusedContainerColor = DjSurfaceElevated,
                    focusedTextColor = DjTextPrimary,
                    unfocusedTextColor = DjTextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Import File Button
            Button(
                onClick = { audioPickerLauncher.launch("audio/*") },
                colors = ButtonDefaults.buttonColors(containerColor = DjSurfaceElevated),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DjGreenActive),
                modifier = Modifier
                    .height(54.dp)
                    .testTag("import_audio_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import", tint = DjGreenActive, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("IMPORT", color = DjGreenActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DjSurface,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = when (selectedTab) {
                        1 -> Color(0xFFA855F7)
                        2 -> DjGreenActive
                        else -> DeckACyan
                    }
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ALL (${tracks.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_all_tracks")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI CREATED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_ai_tracks")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("DEVICE FILES", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_device_tracks")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DeckACyan, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading DJ tracks...", color = DjTextSecondary, fontSize = 12.sp)
                }
            }
        } else if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = DjTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tracks found", color = DjTextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Try importing audio or generating songs in AI Studio", color = DjTextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    TrackListItem(
                        track = track,
                        onLoadDeckA = { viewModel.loadTrackToDeck(DeckId.DECK_A, track) },
                        onLoadDeckB = { viewModel.loadTrackToDeck(DeckId.DECK_B, track) },
                        onOpenTrimmer = { viewModel.openTrackInTrimmer(track, DeckId.DECK_A) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun TrackListItem(
    track: Track,
    onLoadDeckA: () -> Unit,
    onLoadDeckB: () -> Unit,
    onOpenTrimmer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationSec = track.durationMs / 1000
    val durationText = String.format(Locale.US, "%02d:%02d", durationSec / 60, durationSec % 60)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("track_item_${track.id}"),
        colors = CardDefaults.cardColors(containerColor = DjSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title,
                            color = DjTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (track.isAiGenerated) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("AI GEN", color = Color(0xFFA855F7), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${track.artist} • ${track.genre} • Key: ${track.key} • $durationText",
                        color = DjTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // BPM Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF222838))
                        .border(1.dp, DjSurfaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${track.bpm} BPM",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini waveform preview bar
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
                height = 24.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Load to Deck Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onLoadDeckA,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("load_deck_a_${track.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeckACyan.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeckACyan),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("LOAD DECK A", color = DeckACyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onLoadDeckB,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("load_deck_b_${track.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeckBAmber.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeckBAmber),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("LOAD DECK B", color = DeckBAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenTrimmer,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("trim_track_${track.id}"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DjSurfaceBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Trim", tint = DjTextSecondary, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}
