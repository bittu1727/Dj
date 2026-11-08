package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiGenerateScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MixerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrimScreen
import com.example.ui.theme.AiDjMixerTheme
import com.example.ui.theme.DeckACyan
import com.example.ui.theme.DeckBAmber
import com.example.ui.theme.DjBackground
import com.example.ui.theme.DjGreenActive
import com.example.ui.theme.DjRedCue
import com.example.ui.theme.DjSurface
import com.example.ui.theme.DjSurfaceBorder
import com.example.ui.theme.DjSurfaceElevated
import com.example.ui.theme.DjTextMuted
import com.example.ui.theme.DjTextPrimary
import com.example.ui.theme.DjTextSecondary
import com.example.viewmodel.DjViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiDjMixerTheme {
                DjAppRoot()
            }
        }
    }
}

@Composable
fun DjAppRoot(viewModel: DjViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val mixerState by viewModel.mixerState.collectAsState()

    // Handle back button to return to Mixer screen if on secondary tab
    BackHandler(enabled = currentTab != DjViewModel.AppTab.MIXER) {
        viewModel.selectTab(DjViewModel.AppTab.MIXER)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DjBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Sleek Horizontal Console Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(DjSurface)
                .border(1.dp, DjSurfaceBorder)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = DeckACyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI DJ MIXER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DjTextPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E2436))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DeckBAmber)
                }
            }

            // Console Tab Switcher Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabChip(
                    label = "MIXER",
                    icon = Icons.Default.Tune,
                    isSelected = currentTab == DjViewModel.AppTab.MIXER,
                    activeColor = DeckACyan,
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.MIXER) },
                    testTag = "nav_mixer"
                )

                NavTabChip(
                    label = "LIBRARY",
                    icon = Icons.Default.LibraryMusic,
                    isSelected = currentTab == DjViewModel.AppTab.LIBRARY,
                    activeColor = DeckACyan,
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.LIBRARY) },
                    testTag = "nav_library"
                )

                NavTabChip(
                    label = "TRIM",
                    icon = Icons.Default.ContentCut,
                    isSelected = currentTab == DjViewModel.AppTab.TRIM,
                    activeColor = DeckACyan,
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.TRIM) },
                    testTag = "nav_trim"
                )

                NavTabChip(
                    label = "AI STUDIO",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = currentTab == DjViewModel.AppTab.AI_GENERATOR,
                    activeColor = Color(0xFFA855F7),
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.AI_GENERATOR) },
                    testTag = "nav_ai_studio"
                )

                NavTabChip(
                    label = "SETTINGS",
                    icon = Icons.Default.Settings,
                    isSelected = currentTab == DjViewModel.AppTab.SETTINGS,
                    activeColor = DeckACyan,
                    onClick = { viewModel.selectTab(DjViewModel.AppTab.SETTINGS) },
                    testTag = "nav_settings"
                )
            }

            // Quick Status Info on Right
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mixerState.isRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DjRedCue)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("REC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Text(
                        "44.1kHz • 16-BIT",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DjTextMuted
                    )
                }
            }
        }

        // Main Screen Viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Crossfade(targetState = currentTab, label = "screen_fade") { tab ->
                when (tab) {
                    DjViewModel.AppTab.MIXER -> MixerScreen(viewModel = viewModel)
                    DjViewModel.AppTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    DjViewModel.AppTab.TRIM -> TrimScreen(viewModel = viewModel)
                    DjViewModel.AppTab.AI_GENERATOR -> AiGenerateScreen(viewModel = viewModel)
                    DjViewModel.AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NavTabChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else DjTextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DjTextMuted
            )
        }
    }
}
