package com.example.model

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String = "AI DJ Studio",
    val durationMs: Long = 30000L,
    val bpm: Int = 128,
    val key: String = "Am",
    val genre: String = "Electronic",
    val uri: Uri? = null,
    val filePath: String? = null,
    val isAsset: Boolean = false,
    val assetName: String = "",
    val waveform: List<Float> = emptyList(),
    val isAiGenerated: Boolean = false
)

enum class DeckId {
    DECK_A,
    DECK_B
}

data class DeckState(
    val deckId: DeckId,
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val pitchRate: Float = 1.0f, // 0.8f to 1.2f (-20% to +20%)
    val volume: Float = 0.85f,   // 0.0f to 1.0f
    val eqLow: Float = 0.0f,     // -1.0 to 1.0 (-12dB to +12dB)
    val eqMid: Float = 0.0f,     // -1.0 to 1.0
    val eqHigh: Float = 0.0f,    // -1.0 to 1.0
    val cuePointMs: Long = 0L,
    val loopStartMs: Long = 0L,
    val loopEndMs: Long = 0L,
    val isLoopActive: Boolean = false,
    val isSynced: Boolean = false,
    val vuLevel: Float = 0.0f,
    val jogAngleDegrees: Float = 0.0f
)

data class MixerState(
    val crossfader: Float = 0.0f, // -1.0f (Full Deck A) to +1.0f (Full Deck B)
    val masterVolume: Float = 0.9f,
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val lastRecordedFilePath: String? = null,
    val isAutoMixActive: Boolean = false,
    val autoMixTarget: DeckId = DeckId.DECK_B,
    val autoMixProgress: Float = 0.0f,
    val crossfaderCurve: CrossfaderCurve = CrossfaderCurve.SMOOTH
)

enum class CrossfaderCurve {
    SMOOTH,
    SHARP,
    LINEAR
}

data class AiPromptConfig(
    val prompt: String = "",
    val genre: String = "Tech House",
    val mood: String = "Energetic",
    val bpm: Int = 126,
    val durationSeconds: Int = 20,
    val includeBassline: Boolean = true,
    val includeLeadSynth: Boolean = true,
    val includeDrums: Boolean = true
)
