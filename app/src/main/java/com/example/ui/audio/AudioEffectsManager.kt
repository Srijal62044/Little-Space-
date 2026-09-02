package com.example.ui.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Build
import android.util.Log

data class RemixState(
    val bass: Float = 0f,       // -100 to +100
    val treble: Float = 0f,     // -100 to +100
    val vocal: Float = 0f,      // -100 to +100
    val reverb: Float = 0f,     // 0 to 100
    val echoDelay: Float = 0f,  // 0 to 100
    val speed: Float = 1.0f,    // 0.5 to 2.0
    val pitch: Float = 1.0f,    // 0.5 to 2.0
    val volume: Float = 100f,   // 0 to 100
    val balance: Float = 0f,    // -100 (left) to +100 (right)
    val activePresetName: String? = null
)

class AudioEffectsManager(private val context: Context) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null

    private var currentSessionId: Int = 0

    fun attachToSession(sessionId: Int, mediaPlayer: MediaPlayer?, state: RemixState) {
        if (sessionId == 0 || sessionId == currentSessionId && equalizer != null) {
            applyAll(mediaPlayer, state)
            return
        }

        releaseEffects()
        currentSessionId = sessionId

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Equalizer init failed: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "BassBoost init failed: ${e.message}")
        }

        try {
            presetReverb = PresetReverb(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "PresetReverb init failed: ${e.message}")
        }

        applyAll(mediaPlayer, state)
    }

    fun applyAll(mediaPlayer: MediaPlayer?, state: RemixState) {
        applyPlaybackParams(mediaPlayer, state.speed, state.pitch)
        applyVolumeAndBalance(mediaPlayer, state.volume, state.balance)
        applyBass(state.bass)
        applyTreble(state.treble)
        applyVocal(state.vocal)
        applyReverb(state.reverb)
    }

    fun applyPlaybackParams(mediaPlayer: MediaPlayer?, speed: Float, pitch: Float) {
        if (mediaPlayer == null) return
        try {
            val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            val params = PlaybackParams().apply {
                this.speed = clampedSpeed
                this.pitch = clampedPitch
            }
            mediaPlayer.playbackParams = params
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "PlaybackParams error: ${e.message}")
        }
    }

    fun applyVolumeAndBalance(mediaPlayer: MediaPlayer?, volumePercent: Float, balance: Float) {
        if (mediaPlayer == null) return
        try {
            val masterVol = (volumePercent / 100f).coerceIn(0f, 1f)
            // balance: -100 (left only) to 0 (center) to +100 (right only)
            val leftRatio = if (balance <= 0) 1f else (100f - balance) / 100f
            val rightRatio = if (balance >= 0) 1f else (100f + balance) / 100f

            val leftVol = masterVol * leftRatio
            val rightVol = masterVol * rightRatio

            mediaPlayer.setVolume(leftVol, rightVol)
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "SetVolume error: ${e.message}")
        }
    }

    fun applyBass(bassLevel: Float) {
        // bassLevel: -100 to +100
        try {
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    val strength = (bassLevel.coerceAtLeast(0f) * 10f).toInt().coerceIn(0, 1000)
                    bb.setStrength(strength.toShort())
                }
            }

            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                if (numBands > 0) {
                    val range = eq.bandLevelRange // [min, max] typically [-1500, +1500] mB
                    val min = range[0]
                    val max = range[1]
                    val norm = (bassLevel / 100f).coerceIn(-1f, 1f)
                    val targetLevel = if (norm >= 0) (norm * max).toInt() else (-norm * min).toInt()
                    eq.setBandLevel(0.toShort(), targetLevel.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "ApplyBass error: ${e.message}")
        }
    }

    fun applyTreble(trebleLevel: Float) {
        // trebleLevel: -100 to +100
        try {
            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                if (numBands >= 2) {
                    val topBand = (numBands - 1).toShort()
                    val range = eq.bandLevelRange
                    val min = range[0]
                    val max = range[1]
                    val norm = (trebleLevel / 100f).coerceIn(-1f, 1f)
                    val targetLevel = if (norm >= 0) (norm * max).toInt() else (-norm * min).toInt()
                    eq.setBandLevel(topBand, targetLevel.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "ApplyTreble error: ${e.message}")
        }
    }

    fun applyVocal(vocalLevel: Float) {
        // vocalLevel: -100 to +100 affects mid frequencies (around 1kHz - 3kHz)
        try {
            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                if (numBands >= 3) {
                    val midBand = (numBands / 2).toShort()
                    val range = eq.bandLevelRange
                    val min = range[0]
                    val max = range[1]
                    val norm = (vocalLevel / 100f).coerceIn(-1f, 1f)
                    val targetLevel = if (norm >= 0) (norm * max).toInt() else (-norm * min).toInt()
                    eq.setBandLevel(midBand, targetLevel.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "ApplyVocal error: ${e.message}")
        }
    }

    fun applyReverb(reverbPercent: Float) {
        try {
            presetReverb?.let { pr ->
                val preset = when {
                    reverbPercent < 15f -> PresetReverb.PRESET_NONE
                    reverbPercent < 35f -> PresetReverb.PRESET_SMALLROOM
                    reverbPercent < 60f -> PresetReverb.PRESET_MEDIUMROOM
                    reverbPercent < 85f -> PresetReverb.PRESET_LARGEROOM
                    else -> PresetReverb.PRESET_PLATE
                }
                pr.preset = preset
            }
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "ApplyReverb error: ${e.message}")
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            presetReverb?.release()
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Release effects error: ${e.message}")
        } finally {
            equalizer = null
            bassBoost = null
            presetReverb = null
            currentSessionId = 0
        }
    }
}
