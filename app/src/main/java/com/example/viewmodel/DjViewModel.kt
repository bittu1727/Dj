package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiMusicService
import com.example.audio.AudioEngine
import com.example.audio.SynthAudioGenerator
import com.example.audio.WaveformAnalyzer
import com.example.model.AiPromptConfig
import com.example.model.CrossfaderCurve
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.MixerState
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class DjViewModel(application: Application) : AndroidViewModel(application) {

    val audioEngine = AudioEngine(application, viewModelScope)
    val aiMusicService = AiMusicService(application)

    val deckAState: StateFlow<DeckState> = audioEngine.deckAState
    val deckBState: StateFlow<DeckState> = audioEngine.deckBState
    val mixerState: StateFlow<MixerState> = audioEngine.mixerState

    // Navigation Tab state
    enum class AppTab {
        MIXER,
        LIBRARY,
        TRIM,
        AI_GENERATOR,
        SETTINGS
    }

    private val _currentTab = MutableStateFlow(AppTab.MIXER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Track Library
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLibraryLoading = MutableStateFlow(true)
    val isLibraryLoading: StateFlow<Boolean> = _isLibraryLoading.asStateFlow()

    // AI Generation UI state
    private val _aiConfig = MutableStateFlow(AiPromptConfig())
    val aiConfig: StateFlow<AiPromptConfig> = _aiConfig.asStateFlow()

    private val _aiGenState = MutableStateFlow<AiMusicService.GenerationState>(AiMusicService.GenerationState.Idle)
    val aiGenState: StateFlow<AiMusicService.GenerationState> = _aiGenState.asStateFlow()

    // Trimming / Cutter State
    data class TrimState(
        val track: Track? = null,
        val targetDeck: DeckId = DeckId.DECK_A,
        val startMs: Long = 0L,
        val endMs: Long = 10000L,
        val durationMs: Long = 10000L,
        val isPreviewPlaying: Boolean = false,
        val isLooping: Boolean = true,
        val statusMessage: String? = null
    )

    private val _trimState = MutableStateFlow(TrimState())
    val trimState: StateFlow<TrimState> = _trimState.asStateFlow()

    // Tap tempo timestamps
    private val tapHistory = mutableListOf<Long>()
    private val _tapBpm = MutableStateFlow<Int?>(null)
    val tapBpm: StateFlow<Int?> = _tapBpm.asStateFlow()

    // Recorded mix files list
    private val _recordedMixes = MutableStateFlow<List<File>>(emptyList())
    val recordedMixes: StateFlow<List<File>> = _recordedMixes.asStateFlow()

    init {
        initializeLibrary()
        refreshRecordingsList()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    private fun initializeLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLibraryLoading.value = true
            val initial = SynthAudioGenerator.getInitialBundledTracks(getApplication())
            _tracks.value = initial
            _isLibraryLoading.value = false

            // Auto-load Track 1 onto Deck A and Track 2 onto Deck B for instant hands-on DJing!
            if (initial.isNotEmpty()) {
                audioEngine.loadTrack(DeckId.DECK_A, initial[0])
            }
            if (initial.size > 1) {
                audioEngine.loadTrack(DeckId.DECK_B, initial[1])
            }
        }
    }

    fun loadTrackToDeck(deckId: DeckId, track: Track) {
        audioEngine.loadTrack(deckId, track)
        _currentTab.value = AppTab.MIXER
    }

    fun openTrackInTrimmer(track: Track, targetDeck: DeckId) {
        val duration = track.durationMs
        _trimState.value = TrimState(
            track = track,
            targetDeck = targetDeck,
            startMs = 0L,
            endMs = (duration / 2).coerceAtLeast(3000L),
            durationMs = duration
        )
        _currentTab.value = AppTab.TRIM
    }

    fun updateTrimPoints(startMs: Long, endMs: Long) {
        _trimState.update {
            it.copy(
                startMs = startMs.coerceIn(0L, it.durationMs),
                endMs = endMs.coerceIn(startMs + 500L, it.durationMs)
            )
        }
    }

    fun applyTrimToDeck() {
        val current = _trimState.value
        val track = current.track ?: return
        audioEngine.setLoopPoints(current.targetDeck, current.startMs, current.endMs)
        audioEngine.setCuePoint(current.targetDeck, current.startMs)
        _trimState.update { it.copy(statusMessage = "Applied loop & cue to ${current.targetDeck.name}!") }
    }

    fun importLocalAudio(uri: Uri, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val track = Track(
                id = "local_${UUID.randomUUID().toString().take(8)}",
                title = title.substringBeforeLast("."),
                artist = "Local File",
                durationMs = 60000L, // Placeholder until loaded
                bpm = 125,
                genre = "Audio Import",
                uri = uri,
                waveform = List(120) { (0.2f + 0.6f * kotlin.math.sin(it * 0.2f)).coerceIn(0.1f, 0.95f) }
            )
            _tracks.update { listOf(track) + it }
        }
    }

    fun updateAiConfig(config: AiPromptConfig) {
        _aiConfig.value = config
    }

    fun generateAiSong() {
        viewModelScope.launch(Dispatchers.IO) {
            val generated = aiMusicService.generateAiTrack(_aiConfig.value) { state ->
                _aiGenState.value = state
            }
            if (generated != null) {
                _tracks.update { listOf(generated) + it }
            }
        }
    }

    fun registerTapTempo() {
        val now = System.currentTimeMillis()
        tapHistory.add(now)
        if (tapHistory.size > 8) tapHistory.removeAt(0)
        val calculated = WaveformAnalyzer.calculateTapBpm(tapHistory)
        if (calculated != null) {
            _tapBpm.value = calculated
        }
    }

    fun toggleRecording(): String? {
        val res = audioEngine.toggleRecording()
        refreshRecordingsList()
        return res
    }

    fun refreshRecordingsList() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(getApplication<Application>().filesDir, "dj_recordings")
            if (dir.exists()) {
                val files = dir.listFiles { f -> f.extension == "wav" }?.sortedByDescending { it.lastModified() }
                _recordedMixes.value = files ?: emptyList()
            }
        }
    }

    fun shareMix(file: File) {
        audioEngine.mixRecorder.shareRecording(file)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
