package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import com.example.model.CrossfaderCurve
import com.example.model.DeckId
import com.example.model.DeckState
import com.example.model.MixerState
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class AudioEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val _deckAState = MutableStateFlow(DeckState(deckId = DeckId.DECK_A))
    val deckAState: StateFlow<DeckState> = _deckAState.asStateFlow()

    private val _deckBState = MutableStateFlow(DeckState(deckId = DeckId.DECK_B))
    val deckBState: StateFlow<DeckState> = _deckBState.asStateFlow()

    private val _mixerState = MutableStateFlow(MixerState())
    val mixerState: StateFlow<MixerState> = _mixerState.asStateFlow()

    private var playerA: MediaPlayer? = null
    private var equalizerA: Equalizer? = null

    private var playerB: MediaPlayer? = null
    private var equalizerB: Equalizer? = null

    val mixRecorder = MixRecorder(context)

    private var playbackLoopJob: Job? = null
    private var autoMixJob: Job? = null

    init {
        startStatePollingLoop()
    }

    private fun startStatePollingLoop() {
        playbackLoopJob?.cancel()
        playbackLoopJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                updateDeckProgress(DeckId.DECK_A)
                updateDeckProgress(DeckId.DECK_B)

                if (mixRecorder.isRecordingActive()) {
                    _mixerState.update {
                        it.copy(recordingDurationMs = mixRecorder.getRecordingDurationMs())
                    }
                }

                delay(40) // ~25Hz refresh for ultra-smooth jog wheels and meters
            }
        }
    }

    private fun updateDeckProgress(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState

        if (player != null && player.isPlaying) {
            try {
                val currentPos = player.currentPosition.toLong()
                val duration = player.duration.toLong().coerceAtLeast(1L)
                val currentState = stateFlow.value

                // Handle looping boundary
                if (currentState.isLoopActive && currentState.loopEndMs > currentState.loopStartMs) {
                    if (currentPos >= currentState.loopEndMs) {
                        player.seekTo(currentState.loopStartMs.toInt())
                        return
                    }
                }

                // Smooth jog wheel rotation
                val angleDelta = (currentState.pitchRate * 4.5f)
                val newAngle = (currentState.jogAngleDegrees + angleDelta) % 360f

                // Simulated VU peak level
                val rawGain = currentState.volume * getEffectiveDeckGain(deckId)
                val eqBoost = (currentState.eqLow + currentState.eqMid + currentState.eqHigh) / 6f
                val jitter = (sin(currentPos * 0.02) * 0.15f + 0.85f).toFloat()
                val vu = (rawGain * (1.0f + eqBoost) * jitter).coerceIn(0.05f, 1.0f)

                stateFlow.update {
                    it.copy(
                        currentPositionMs = currentPos,
                        durationMs = duration,
                        jogAngleDegrees = newAngle,
                        vuLevel = vu
                    )
                }
            } catch (e: Exception) {
                // Player state transition safety
            }
        } else {
            stateFlow.update {
                it.copy(vuLevel = max(0f, it.vuLevel - 0.1f))
            }
        }
    }

    fun loadTrack(deckId: DeckId, track: Track) {
        scope.launch(Dispatchers.Main) {
            val player = if (deckId == DeckId.DECK_A) playerA else playerB
            val oldEq = if (deckId == DeckId.DECK_A) equalizerA else equalizerB

            try {
                player?.stop()
                player?.release()
                oldEq?.release()
            } catch (e: Exception) {
                // Ignore cleanup
            }

            val newPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }

            try {
                if (track.filePath != null) {
                    newPlayer.setDataSource(track.filePath)
                } else if (track.uri != null) {
                    newPlayer.setDataSource(context, track.uri)
                }
                newPlayer.prepare()

                // Try attach equalizer effect
                val newEq = try {
                    Equalizer(0, newPlayer.audioSessionId).apply {
                        enabled = true
                    }
                } catch (e: Exception) {
                    null
                }

                if (deckId == DeckId.DECK_A) {
                    playerA = newPlayer
                    equalizerA = newEq
                    _deckAState.update {
                        it.copy(
                            track = track,
                            isPlaying = false,
                            currentPositionMs = 0L,
                            durationMs = newPlayer.duration.toLong(),
                            cuePointMs = 0L,
                            loopStartMs = 0L,
                            loopEndMs = (newPlayer.duration.toLong() / 2).coerceAtLeast(4000L),
                            isLoopActive = false
                        )
                    }
                } else {
                    playerB = newPlayer
                    equalizerB = newEq
                    _deckBState.update {
                        it.copy(
                            track = track,
                            isPlaying = false,
                            currentPositionMs = 0L,
                            durationMs = newPlayer.duration.toLong(),
                            cuePointMs = 0L,
                            loopStartMs = 0L,
                            loopEndMs = (newPlayer.duration.toLong() / 2).coerceAtLeast(4000L),
                            isLoopActive = false
                        )
                    }
                }

                applyDeckVolumes()
                applyDeckEqualizer(deckId)
                applyPlaybackRate(deckId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlay(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        val current = stateFlow.value

        if (player == null) return

        try {
            if (player.isPlaying) {
                player.pause()
                stateFlow.update { it.copy(isPlaying = false) }
            } else {
                player.start()
                stateFlow.update { it.copy(isPlaying = true) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cueDeck(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        val current = stateFlow.value

        if (player == null) return

        try {
            if (player.isPlaying) {
                player.pause()
                player.seekTo(current.cuePointMs.toInt())
                stateFlow.update { it.copy(isPlaying = false, currentPositionMs = current.cuePointMs) }
            } else {
                // If already at cue point, set new cue point at current position
                if (kotlin.math.abs(current.currentPositionMs - current.cuePointMs) < 100) {
                    val newCue = current.currentPositionMs
                    stateFlow.update { it.copy(cuePointMs = newCue) }
                } else {
                    // Jump to cue point
                    player.seekTo(current.cuePointMs.toInt())
                    stateFlow.update { it.copy(currentPositionMs = current.cuePointMs) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCuePoint(deckId: DeckId, positionMs: Long) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        stateFlow.update { it.copy(cuePointMs = positionMs) }
    }

    fun seekTo(deckId: DeckId, positionMs: Long) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        try {
            player?.seekTo(positionMs.toInt())
            stateFlow.update { it.copy(currentPositionMs = positionMs) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPitchRate(deckId: DeckId, rate: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        val clampedRate = rate.coerceIn(0.8f, 1.2f)
        stateFlow.update { it.copy(pitchRate = clampedRate, isSynced = false) }
        applyPlaybackRate(deckId)
    }

    fun nudgePitch(deckId: DeckId, deltaPitch: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        val newRate = (stateFlow.value.pitchRate + deltaPitch).coerceIn(0.8f, 1.2f)
        stateFlow.update { it.copy(pitchRate = newRate) }
        applyPlaybackRate(deckId)
    }

    fun syncDecks(sourceDeckId: DeckId) {
        val targetDeckId = if (sourceDeckId == DeckId.DECK_A) DeckId.DECK_B else DeckId.DECK_A
        val sourceTrack = if (sourceDeckId == DeckId.DECK_A) _deckAState.value.track else _deckBState.value.track
        val targetTrack = if (targetDeckId == DeckId.DECK_A) _deckAState.value.track else _deckBState.value.track

        if (sourceTrack == null || targetTrack == null) return

        val sourceBpm = sourceTrack.bpm * (if (sourceDeckId == DeckId.DECK_A) _deckAState.value.pitchRate else _deckBState.value.pitchRate)
        val targetBaseBpm = targetTrack.bpm.toFloat()

        if (targetBaseBpm > 0) {
            val syncRatio = (sourceBpm / targetBaseBpm).coerceIn(0.8f, 1.2f)
            setPitchRate(targetDeckId, syncRatio)
            if (targetDeckId == DeckId.DECK_A) {
                _deckAState.update { it.copy(isSynced = true) }
            } else {
                _deckBState.update { it.copy(isSynced = true) }
            }
        }
    }

    fun setDeckVolume(deckId: DeckId, volume: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        stateFlow.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
        applyDeckVolumes()
    }

    fun setCrossfader(value: Float) {
        _mixerState.update { it.copy(crossfader = value.coerceIn(-1.0f, 1.0f)) }
        applyDeckVolumes()
    }

    fun setMasterVolume(value: Float) {
        _mixerState.update { it.copy(masterVolume = value.coerceIn(0f, 1f)) }
        applyDeckVolumes()
    }

    fun setCrossfaderCurve(curve: CrossfaderCurve) {
        _mixerState.update { it.copy(crossfaderCurve = curve) }
        applyDeckVolumes()
    }

    fun setEq(deckId: DeckId, low: Float, mid: Float, high: Float) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        stateFlow.update {
            it.copy(
                eqLow = low.coerceIn(-1f, 1f),
                eqMid = mid.coerceIn(-1f, 1f),
                eqHigh = high.coerceIn(-1f, 1f)
            )
        }
        applyDeckEqualizer(deckId)
    }

    fun toggleLoop(deckId: DeckId) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        val current = stateFlow.value
        val newLoopState = !current.isLoopActive

        stateFlow.update {
            if (newLoopState && it.loopEndMs <= it.loopStartMs) {
                // Auto 4-beat loop window (~2 seconds at 120bpm)
                val loopDuration = (60000L / (it.track?.bpm ?: 120)) * 4
                it.copy(
                    isLoopActive = true,
                    loopStartMs = it.currentPositionMs,
                    loopEndMs = min(it.durationMs, it.currentPositionMs + loopDuration)
                )
            } else {
                it.copy(isLoopActive = newLoopState)
            }
        }
    }

    fun setLoopPoints(deckId: DeckId, startMs: Long, endMs: Long) {
        val stateFlow = if (deckId == DeckId.DECK_A) _deckAState else _deckBState
        stateFlow.update {
            it.copy(
                loopStartMs = startMs,
                loopEndMs = max(startMs + 500L, endMs)
            )
        }
    }

    fun triggerAutoMix(targetDeck: DeckId, durationSeconds: Int = 6) {
        autoMixJob?.cancel()
        _mixerState.update {
            it.copy(
                isAutoMixActive = true,
                autoMixTarget = targetDeck,
                autoMixProgress = 0f
            )
        }

        autoMixJob = scope.launch(Dispatchers.Main) {
            val startX = _mixerState.value.crossfader
            val targetX = if (targetDeck == DeckId.DECK_B) 1.0f else -1.0f
            val steps = durationSeconds * 30
            val delayPerStep = (durationSeconds * 1000L) / steps

            // Ensure target deck is playing
            val targetPlayer = if (targetDeck == DeckId.DECK_A) playerA else playerB
            if (targetPlayer != null && !targetPlayer.isPlaying) {
                togglePlay(targetDeck)
            }

            for (step in 1..steps) {
                val progress = step.toFloat() / steps
                val currentX = startX + (targetX - startX) * progress
                setCrossfader(currentX)
                _mixerState.update { it.copy(autoMixProgress = progress) }
                delay(delayPerStep)
            }

            _mixerState.update { it.copy(isAutoMixActive = false, autoMixProgress = 1.0f) }
        }
    }

    fun toggleRecording(): String? {
        val isCurrentlyRecording = _mixerState.value.isRecording
        if (!isCurrentlyRecording) {
            val filePath = mixRecorder.startRecording()
            if (filePath != null) {
                _mixerState.update {
                    it.copy(isRecording = true, recordingDurationMs = 0L, lastRecordedFilePath = filePath)
                }
            }
            return filePath
        } else {
            val file = mixRecorder.stopRecording()
            _mixerState.update {
                it.copy(isRecording = false, lastRecordedFilePath = file?.absolutePath)
            }
            return file?.absolutePath
        }
    }

    private fun getEffectiveDeckGain(deckId: DeckId): Float {
        val x = _mixerState.value.crossfader // -1.0 to 1.0
        val curve = _mixerState.value.crossfaderCurve

        return when (curve) {
            CrossfaderCurve.SMOOTH -> {
                // Equal power crossfade
                if (deckId == DeckId.DECK_A) {
                    val angle = ((1.0 - x) / 2.0) * (Math.PI / 2.0)
                    sin(angle).toFloat()
                } else {
                    val angle = ((1.0 + x) / 2.0) * (Math.PI / 2.0)
                    sin(angle).toFloat()
                }
            }
            CrossfaderCurve.LINEAR -> {
                if (deckId == DeckId.DECK_A) {
                    min(1.0f, 1.0f - x)
                } else {
                    min(1.0f, 1.0f + x)
                }
            }
            CrossfaderCurve.SHARP -> {
                if (deckId == DeckId.DECK_A) {
                    if (x > 0.85f) (1.0f - x) / 0.15f else 1.0f
                } else {
                    if (x < -0.85f) (1.0f + x) / 0.15f else 1.0f
                }
            }
        }.coerceIn(0f, 1f)
    }

    private fun applyDeckVolumes() {
        val master = _mixerState.value.masterVolume
        val gainA = _deckAState.value.volume * getEffectiveDeckGain(DeckId.DECK_A) * master
        val gainB = _deckBState.value.volume * getEffectiveDeckGain(DeckId.DECK_B) * master

        try {
            playerA?.setVolume(gainA, gainA)
        } catch (e: Exception) {}

        try {
            playerB?.setVolume(gainB, gainB)
        } catch (e: Exception) {}
    }

    private fun applyPlaybackRate(deckId: DeckId) {
        val player = if (deckId == DeckId.DECK_A) playerA else playerB
        val state = if (deckId == DeckId.DECK_A) _deckAState.value else _deckBState.value

        if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = state.pitchRate
                player.playbackParams = params
            } catch (e: Exception) {
                // If player is not initialized or error
            }
        }
    }

    private fun applyDeckEqualizer(deckId: DeckId) {
        val eq = if (deckId == DeckId.DECK_A) equalizerA else equalizerB
        val state = if (deckId == DeckId.DECK_A) _deckAState.value else _deckBState.value

        if (eq == null) return

        try {
            val numBands = eq.numberOfBands
            if (numBands >= 3) {
                val minLevel = eq.bandLevelRange[0] // e.g. -1500 mB
                val maxLevel = eq.bandLevelRange[1] // e.g. +1500 mB

                val lowBand = 0.toShort()
                val midBand = (numBands / 2).toShort()
                val highBand = (numBands - 1).toShort()

                val lowMb = ((state.eqLow * maxLevel)).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                val midMb = ((state.eqMid * maxLevel)).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                val highMb = ((state.eqHigh * maxLevel)).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()

                eq.setBandLevel(lowBand, lowMb)
                eq.setBandLevel(midBand, midMb)
                eq.setBandLevel(highBand, highMb)
            }
        } catch (e: Exception) {
            // Equalizer DSP fallback
        }
    }

    fun release() {
        playbackLoopJob?.cancel()
        autoMixJob?.cancel()
        try {
            playerA?.release()
            equalizerA?.release()
            playerB?.release()
            equalizerB?.release()
        } catch (e: Exception) {}
    }
}
