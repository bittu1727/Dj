package com.example.audio

import android.content.Context
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object SynthAudioGenerator {

    private const val SAMPLE_RATE = 44100
    private const val NUM_CHANNELS = 2
    private const val BITS_PER_SAMPLE = 16

    data class SynthParams(
        val bpm: Int = 126,
        val durationSeconds: Int = 24,
        val rootNoteMidi: Int = 57, // A3
        val scaleIntervals: List<Int> = listOf(0, 3, 5, 7, 10), // Minor pentatonic
        val kickPattern: List<Boolean> = listOf(true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false), // 4-on-the-floor
        val snarePattern: List<Boolean> = listOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false),
        val hihatPattern: List<Boolean> = listOf(false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, false), // offbeat hats
        val basslineNotes: List<Int> = listOf(0, 0, 3, 5, 0, 7, 5, 3),
        val leadArpPattern: List<Int> = listOf(0, 7, 10, 12, 7, 10, 12, 15),
        val filterCutoff: Float = 0.65f,
        val distortion: Float = 0.15f
    )

    suspend fun generateTrack(
        context: Context,
        trackId: String,
        title: String,
        artist: String,
        genre: String,
        params: SynthParams,
        isAiGenerated: Boolean = false
    ): Track = withContext(Dispatchers.IO) {
        val tracksDir = File(context.filesDir, "dj_tracks").apply { mkdirs() }
        val outputFile = File(tracksDir, "track_${trackId}.wav")

        val totalSamples = SAMPLE_RATE * params.durationSeconds
        val secondsPerBeat = 60.0 / params.bpm
        val samplesPer16th = (SAMPLE_RATE * secondsPerBeat / 4.0).toInt().coerceAtLeast(1)

        val buffer = ShortArray(totalSamples * NUM_CHANNELS)
        val waveformPeaks = ArrayList<Float>()
        val waveformStep = max(1, totalSamples / 120)

        var peakAccumulator = 0f
        var peakSampleCount = 0

        // Track synthesis state
        val random = Random(trackId.hashCode())

        for (i in 0 until totalSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            val sixteenthIndex = (i / samplesPer16th) % 16
            val barIndex = (i / (samplesPer16th * 16))
            val sampleWithin16th = i % samplesPer16th
            val timeWithin16th = sampleWithin16th.toDouble() / SAMPLE_RATE

            var sampleLeft = 0.0
            var sampleRight = 0.0

            // 1. Kick Drum (Punchy 4-on-the-floor)
            if (params.kickPattern[sixteenthIndex]) {
                val kickDecay = 0.22
                if (timeWithin16th < kickDecay) {
                    val kickEnvelope = exp(-timeWithin16th * 18.0)
                    val kickFreq = 48.0 + 130.0 * exp(-timeWithin16th * 40.0)
                    val kickWave = sin(2.0 * PI * kickFreq * timeWithin16th)
                    val kick = kickWave * kickEnvelope * 0.75
                    sampleLeft += kick
                    sampleRight += kick
                }
            }

            // 2. Snare / Clap
            if (params.snarePattern[sixteenthIndex]) {
                val snareDecay = 0.18
                if (timeWithin16th < snareDecay) {
                    val snareEnv = exp(-timeWithin16th * 24.0)
                    val noise = (random.nextDouble() * 2.0 - 1.0)
                    val snareTone = sin(2.0 * PI * 185.0 * timeWithin16th) * 0.3
                    val snare = (noise * 0.7 + snareTone) * snareEnv * 0.45
                    sampleLeft += snare * 0.95
                    sampleRight += snare * 1.05
                }
            }

            // 3. Hi-Hats
            if (params.hihatPattern[sixteenthIndex]) {
                val hatDecay = 0.065
                if (timeWithin16th < hatDecay) {
                    val hatEnv = exp(-timeWithin16th * 55.0)
                    val noise = (random.nextDouble() * 2.0 - 1.0)
                    val hat = noise * hatEnv * 0.28
                    sampleLeft += hat * 0.8
                    sampleRight += hat * 1.2
                }
            }

            // 4. Synth Bassline
            val bassStep = (i / (samplesPer16th * 2)) % params.basslineNotes.size
            val bassInterval = params.basslineNotes[bassStep]
            val bassMidi = params.rootNoteMidi - 24 + bassInterval
            val bassFreq = midiToFreq(bassMidi)
            val bassEnv = 1.0 - (sampleWithin16th.toDouble() / (samplesPer16th * 2)).coerceIn(0.0, 1.0) * 0.35
            // Saturated Sawtooth bass
            val bassPhase = (time * bassFreq) % 1.0
            val bassWave = (bassPhase * 2.0 - 1.0)
            val bassFiltered = bassWave * params.filterCutoff + sin(2.0 * PI * bassFreq * time) * 0.5
            val bass = bassFiltered * bassEnv * 0.38
            sampleLeft += bass
            sampleRight += bass

            // 5. Lead Synth / Arpeggio
            val arpStep = (i / samplesPer16th) % params.leadArpPattern.size
            val arpInterval = params.leadArpPattern[arpStep]
            val arpMidi = params.rootNoteMidi + arpInterval
            val arpFreq = midiToFreq(arpMidi)
            val arpEnv = exp(-timeWithin16th * 8.0)
            val arpPhase = (time * arpFreq) % 1.0
            val arpWave = sin(2.0 * PI * arpFreq * time) * 0.6 + (arpPhase * 2.0 - 1.0) * 0.4
            // Stereo panning for arpeggio
            val pan = (sin(time * 1.5) * 0.35).coerceIn(-0.35, 0.35)
            val lead = arpWave * arpEnv * 0.28
            sampleLeft += lead * (0.5 - pan)
            sampleRight += lead * (0.5 + pan)

            // Soft clipper / Master limiter
            val clippedL = softClip(sampleLeft)
            val clippedR = softClip(sampleRight)

            val shortL = (clippedL * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            val shortR = (clippedR * 32767.0).toInt().coerceIn(-32768, 32767).toShort()

            buffer[i * 2] = shortL
            buffer[i * 2 + 1] = shortR

            // Waveform calculation
            val currentAmp = max(kotlin.math.abs(clippedL), kotlin.math.abs(clippedR)).toFloat()
            peakAccumulator = max(peakAccumulator, currentAmp)
            peakSampleCount++

            if (peakSampleCount >= waveformStep) {
                waveformPeaks.add(peakAccumulator.coerceIn(0.05f, 1.0f))
                peakAccumulator = 0f
                peakSampleCount = 0
            }
        }

        // Write WAV header and audio data
        writeWavFile(outputFile, buffer, totalSamples)

        val durationMs = (params.durationSeconds * 1000L)

        Track(
            id = trackId,
            title = title,
            artist = artist,
            durationMs = durationMs,
            bpm = params.bpm,
            key = midiToKeyName(params.rootNoteMidi),
            genre = genre,
            filePath = outputFile.absolutePath,
            waveform = waveformPeaks,
            isAiGenerated = isAiGenerated
        )
    }

    private fun midiToFreq(midi: Int): Double = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)

    private fun midiToKeyName(midi: Int): String {
        val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteName = notes[(midi % 12 + 12) % 12]
        return "${noteName}m"
    }

    private fun softClip(x: Double): Double {
        // Smooth hyperbolic tangent soft-saturator
        return when {
            x > 1.2 -> 1.0
            x < -1.2 -> -1.0
            else -> x - (x * x * x) / 4.0
        }
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, totalFrames: Int) {
        val totalAudioLen = totalFrames * NUM_CHANNELS * (BITS_PER_SAMPLE / 8)
        val totalDataLen = totalAudioLen + 36
        val byteRate = SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8)

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

            bb.put("RIFF".toByteArray())
            bb.putInt(totalDataLen)
            bb.put("WAVE".toByteArray())
            bb.put("fmt ".toByteArray())
            bb.putInt(16) // Subchunk1Size for PCM
            bb.putShort(1.toShort()) // AudioFormat 1 = PCM
            bb.putShort(NUM_CHANNELS.toShort())
            bb.putInt(SAMPLE_RATE)
            bb.putInt(byteRate)
            bb.putShort((NUM_CHANNELS * (BITS_PER_SAMPLE / 8)).toShort()) // BlockAlign
            bb.putShort(BITS_PER_SAMPLE.toShort())
            bb.put("data".toByteArray())
            bb.putInt(totalAudioLen)

            out.write(header)

            val pcmBytes = ByteArray(pcmData.size * 2)
            ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmData)
            out.write(pcmBytes)
        }
    }

    suspend fun getInitialBundledTracks(context: Context): List<Track> {
        val list = mutableListOf<Track>()

        // Track 1: Cyber Neon House (Deck A Starter)
        list.add(
            generateTrack(
                context = context,
                trackId = "preset_house_126",
                title = "Cyber Neon House",
                artist = "DJ Pulse",
                genre = "Tech House",
                params = SynthParams(
                    bpm = 126,
                    durationSeconds = 30,
                    rootNoteMidi = 57, // Am
                    filterCutoff = 0.75f,
                    distortion = 0.2f
                )
            )
        )

        // Track 2: Amber Sunset Groove (Deck B Starter)
        list.add(
            generateTrack(
                context = context,
                trackId = "preset_amber_124",
                title = "Amber Sunset Groove",
                artist = "Solar Groove",
                genre = "Deep House",
                params = SynthParams(
                    bpm = 124,
                    durationSeconds = 30,
                    rootNoteMidi = 54, // F#m
                    scaleIntervals = listOf(0, 2, 4, 7, 9),
                    basslineNotes = listOf(0, 4, 7, 9, 7, 4, 2, 0),
                    leadArpPattern = listOf(0, 4, 7, 12, 16, 12, 7, 4),
                    filterCutoff = 0.60f
                )
            )
        )

        // Track 3: Acid Overdrive
        list.add(
            generateTrack(
                context = context,
                trackId = "preset_techno_132",
                title = "Acid Overdrive",
                artist = "Subterranean",
                genre = "Techno",
                params = SynthParams(
                    bpm = 132,
                    durationSeconds = 28,
                    rootNoteMidi = 50, // Dm
                    basslineNotes = listOf(0, 0, 12, 0, 3, 0, 7, 10),
                    filterCutoff = 0.85f,
                    distortion = 0.35f
                )
            )
        )

        // Track 4: Neon Synthwave 80s
        list.add(
            generateTrack(
                context = context,
                trackId = "preset_synthwave_118",
                title = "Neon Synthwave 80s",
                artist = "Retrowave AI",
                genre = "Synthwave",
                params = SynthParams(
                    bpm = 118,
                    durationSeconds = 32,
                    rootNoteMidi = 59, // Bm
                    basslineNotes = listOf(0, 0, 7, 7, 5, 5, 3, 2),
                    leadArpPattern = listOf(0, 7, 12, 14, 15, 14, 12, 7),
                    filterCutoff = 0.7f
                )
            )
        )

        return list
    }
}
