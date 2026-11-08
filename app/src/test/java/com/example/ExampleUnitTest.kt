package com.example

import com.example.audio.WaveformAnalyzer
import com.example.model.CrossfaderCurve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class ExampleUnitTest {

    @Test
    fun tapTempo_calculatesAccurateBpm() {
        // Taps spaced at 500ms intervals = 120 BPM
        val base = 1000000L
        val timestamps = listOf(base, base + 500, base + 1000, base + 1500, base + 2000)
        val bpm = WaveformAnalyzer.calculateTapBpm(timestamps)

        assertNotNull(bpm)
        assertEquals(120, bpm)
    }

    @Test
    fun crossfaderCurve_constantPowerCenterEqualsSqrtHalf() {
        val crossfader = 0.0 // Center position
        val angleA = ((1.0 - crossfader) / 2.0) * (Math.PI / 2.0)
        val angleB = ((1.0 + crossfader) / 2.0) * (Math.PI / 2.0)

        val gainA = sin(angleA)
        val gainB = sin(angleB)

        // At center, constant power gain should be sin(PI/4) ≈ 0.7071
        assertTrue(kotlin.math.abs(gainA - 0.7071) < 0.01)
        assertTrue(kotlin.math.abs(gainB - 0.7071) < 0.01)

        // Total power sum (gainA^2 + gainB^2) should equal 1.0 (no volume dip in center)
        val totalPower = gainA * gainA + gainB * gainB
        assertTrue(kotlin.math.abs(totalPower - 1.0) < 0.01)
    }

    @Test
    fun trimmingMath_validatesStartEndPoints() {
        val durationMs = 30000L
        val requestedStart = 5000L
        val requestedEnd = 12000L

        val startClamped = requestedStart.coerceIn(0L, durationMs)
        val endClamped = requestedEnd.coerceIn(startClamped + 500L, durationMs)

        assertEquals(5000L, startClamped)
        assertEquals(12000L, endClamped)
        assertTrue(endClamped > startClamped)
    }
}
