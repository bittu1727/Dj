package com.example.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object WaveformAnalyzer {

    /**
     * Extracts normalized amplitude peaks (0.0f to 1.0f) for waveform rendering.
     */
    suspend fun extractWaveform(filePath: String, targetPoints: Int = 120): List<Float> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() < 44) {
            return@withContext List(targetPoints) { (0.2f + 0.6f * kotlin.math.sin(it * 0.15f)).coerceIn(0.1f, 0.9f) }
        }

        try {
            FileInputStream(file).use { fis ->
                val bytes = fis.readBytes()
                if (bytes.size < 44) return@withContext emptyList()

                // Skip 44-byte WAV header
                val dataSize = bytes.size - 44
                val totalShorts = dataSize / 2
                if (totalShorts <= 0) return@withContext emptyList()

                val shortBuffer = ByteBuffer.wrap(bytes, 44, dataSize)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()

                val step = max(1, totalShorts / targetPoints)
                val peaks = ArrayList<Float>(targetPoints)

                var maxVal = 0f
                var count = 0

                for (i in 0 until totalShorts) {
                    val sample = shortBuffer.get(i).toInt()
                    val norm = abs(sample) / 32768f
                    if (norm > maxVal) maxVal = norm
                    count++

                    if (count >= step) {
                        peaks.add(maxVal.coerceIn(0.05f, 1.0f))
                        maxVal = 0f
                        count = 0
                        if (peaks.size >= targetPoints) break
                    }
                }

                while (peaks.size < targetPoints) {
                    peaks.add(peaks.lastOrNull() ?: 0.2f)
                }

                return@withContext peaks
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext List(targetPoints) { 0.3f }
        }
    }

    /**
     * Detects approximate BPM from WAV audio buffer via onset energy analysis.
     */
    suspend fun detectBpm(filePath: String, defaultBpm: Int = 126): Int = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() < 44) return@withContext defaultBpm

        try {
            FileInputStream(file).use { fis ->
                val bytes = fis.readBytes()
                val dataSize = bytes.size - 44
                val totalShorts = dataSize / 2
                if (totalShorts < 44100) return@withContext defaultBpm

                val sampleRate = 44100
                // Analyze first 10 seconds
                val maxSamples = min(totalShorts, sampleRate * 10)
                val shortBuffer = ByteBuffer.wrap(bytes, 44, maxSamples * 2)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()

                // Calculate energy chunks every 50ms (20 chunks per second)
                val chunkSize = sampleRate / 20
                val numChunks = maxSamples / chunkSize
                val energies = FloatArray(numChunks)

                for (c in 0 until numChunks) {
                    var sum = 0.0
                    for (s in 0 until chunkSize) {
                        val sample = shortBuffer.get(c * chunkSize + s).toDouble()
                        sum += sample * sample
                    }
                    energies[c] = (sum / chunkSize).toFloat()
                }

                // Find energy peaks (onsets)
                val onsets = ArrayList<Int>()
                for (i in 1 until numChunks - 1) {
                    if (energies[i] > energies[i - 1] && energies[i] > energies[i + 1] && energies[i] > 1000000f) {
                        onsets.add(i)
                    }
                }

                if (onsets.size < 4) return@withContext defaultBpm

                // Histogram of delta intervals between onsets
                val intervals = ArrayList<Float>()
                for (i in 0 until onsets.size - 1) {
                    val deltaSec = (onsets[i + 1] - onsets[i]) * 0.05f
                    // Filter plausible beat intervals: 70 BPM (0.85s) to 180 BPM (0.33s)
                    if (deltaSec in 0.33f..0.86f) {
                        intervals.add(deltaSec)
                    }
                }

                if (intervals.isEmpty()) return@withContext defaultBpm

                val avgInterval = intervals.average().toFloat()
                val detectedBpm = (60.0f / avgInterval).toInt()
                return@withContext detectedBpm.coerceIn(80, 160)
            }
        } catch (e: Exception) {
            return@withContext defaultBpm
        }
    }

    /**
     * Tap tempo calculator based on timestamp history.
     */
    fun calculateTapBpm(tapTimestamps: List<Long>): Int? {
        if (tapTimestamps.size < 3) return null
        val diffs = mutableListOf<Long>()
        for (i in 1 until tapTimestamps.size) {
            val delta = tapTimestamps[i] - tapTimestamps[i - 1]
            if (delta in 250..2000) { // Valid tap interval between 30 and 240 BPM
                diffs.add(delta)
            }
        }
        if (diffs.isEmpty()) return null
        val avgDiffMs = diffs.average()
        val calculatedBpm = (60000.0 / avgDiffMs).toInt()
        return calculatedBpm.coerceIn(60, 200)
    }
}
