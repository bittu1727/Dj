package com.example.ai

import android.content.Context
import com.example.audio.SynthAudioGenerator
import com.example.model.AiPromptConfig
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class AiMusicService(private val context: Context) {

    sealed class GenerationState {
        object Idle : GenerationState()
        data class Generating(val stepMessage: String, val progress: Float) : GenerationState()
        data class Success(val track: Track) : GenerationState()
        data class Error(val message: String) : GenerationState()
    }

    suspend fun generateAiTrack(
        config: AiPromptConfig,
        onProgress: (GenerationState) -> Unit
    ): Track? = withContext(Dispatchers.IO) {
        try {
            onProgress(GenerationState.Generating("Analyzing prompt & musical key...", 0.15f))
            delay(400)

            // Select musical parameters based on genre & mood
            val (rootMidi, intervals, bassNotes, leadPattern, filterCutoff, distortion) = deriveMusicalParams(config)

            onProgress(GenerationState.Generating("Generating rhythm stems & drum arrangement...", 0.45f))
            delay(500)

            val kickPattern = deriveKickPattern(config.genre)
            val snarePattern = deriveSnarePattern(config.genre)
            val hihatPattern = deriveHihatPattern(config.genre)

            onProgress(GenerationState.Generating("Synthesizing multi-track audio & bassline...", 0.75f))
            delay(400)

            val trackId = "ai_${UUID.randomUUID().toString().take(8)}"
            val generatedTitle = if (config.prompt.isNotBlank()) {
                val clean = config.prompt.take(24).trim().split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                "$clean (${config.genre})"
            } else {
                "${config.mood} ${config.genre} Track"
            }

            val synthParams = SynthAudioGenerator.SynthParams(
                bpm = config.bpm,
                durationSeconds = config.durationSeconds,
                rootNoteMidi = rootMidi,
                scaleIntervals = intervals,
                kickPattern = kickPattern,
                snarePattern = snarePattern,
                hihatPattern = hihatPattern,
                basslineNotes = bassNotes,
                leadArpPattern = leadPattern,
                filterCutoff = filterCutoff,
                distortion = distortion
            )

            val track = SynthAudioGenerator.generateTrack(
                context = context,
                trackId = trackId,
                title = generatedTitle,
                artist = "Gemini AI Producer",
                genre = config.genre,
                params = synthParams,
                isAiGenerated = true
            )

            onProgress(GenerationState.Generating("Rendering final mixdown & waveform...", 0.95f))
            delay(300)

            onProgress(GenerationState.Success(track))
            return@withContext track
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(GenerationState.Error("Generation error: ${e.localizedMessage ?: "Unknown error"}"))
            return@withContext null
        }
    }

    private data class ParamsTuple(
        val rootMidi: Int,
        val intervals: List<Int>,
        val bassNotes: List<Int>,
        val leadPattern: List<Int>,
        val filterCutoff: Float,
        val distortion: Float
    )

    private fun deriveMusicalParams(config: AiPromptConfig): ParamsTuple {
        val promptLower = config.prompt.lowercase()

        return when (config.genre.lowercase()) {
            "techno" -> {
                ParamsTuple(
                    rootMidi = 50, // D3
                    intervals = listOf(0, 1, 3, 5, 7, 8, 10), // Phrygian
                    bassNotes = listOf(0, 0, 12, 0, 1, 0, 0, 3),
                    leadPattern = listOf(0, 3, 7, 10, 12, 10, 7, 3),
                    filterCutoff = 0.8f,
                    distortion = 0.35f
                )
            }
            "synthwave" -> {
                ParamsTuple(
                    rootMidi = 59, // B3
                    intervals = listOf(0, 2, 3, 5, 7, 8, 10), // Natural minor
                    bassNotes = listOf(0, 0, 7, 7, 8, 8, 5, 3),
                    leadPattern = listOf(0, 7, 12, 15, 14, 12, 7, 3),
                    filterCutoff = 0.65f,
                    distortion = 0.12f
                )
            }
            "hip-hop", "boom bap" -> {
                ParamsTuple(
                    rootMidi = 55, // G3
                    intervals = listOf(0, 3, 5, 6, 7, 10), // Blues scale
                    bassNotes = listOf(0, 3, 5, 6, 5, 3, 0, 0),
                    leadPattern = listOf(0, 10, 7, 5, 3, 5, 7, 10),
                    filterCutoff = 0.55f,
                    distortion = 0.1f
                )
            }
            "ambient", "chill" -> {
                ParamsTuple(
                    rootMidi = 52, // E3
                    intervals = listOf(0, 2, 4, 7, 9), // Major pentatonic
                    bassNotes = listOf(0, 4, 7, 9, 7, 4, 2, 0),
                    leadPattern = listOf(0, 4, 7, 11, 14, 11, 7, 4),
                    filterCutoff = 0.45f,
                    distortion = 0.05f
                )
            }
            else -> { // Tech House / Default
                ParamsTuple(
                    rootMidi = 57, // A3
                    intervals = listOf(0, 3, 5, 7, 10), // Minor pentatonic
                    bassNotes = listOf(0, 0, 3, 5, 0, 7, 5, 3),
                    leadPattern = listOf(0, 7, 10, 12, 7, 10, 12, 15),
                    filterCutoff = 0.72f,
                    distortion = 0.18f
                )
            }
        }
    }

    private fun deriveKickPattern(genre: String): List<Boolean> {
        return when (genre.lowercase()) {
            "hip-hop" -> listOf(true, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false)
            else -> listOf(true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false) // 4-on-the-floor
        }
    }

    private fun deriveSnarePattern(genre: String): List<Boolean> {
        return when (genre.lowercase()) {
            "hip-hop" -> listOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false)
            else -> listOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false)
        }
    }

    private fun deriveHihatPattern(genre: String): List<Boolean> {
        return when (genre.lowercase()) {
            "techno" -> listOf(false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, false)
            else -> listOf(false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, true)
        }
    }
}
