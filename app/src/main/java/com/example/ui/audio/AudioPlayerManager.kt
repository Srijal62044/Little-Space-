package com.example.ui.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.data.local.entity.RemixPresetEntity
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class VisualizerType {
    WAVEFORM, BARS, CIRCULAR
}

class AudioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private val effectsManager = AudioEffectsManager(context)

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<SongEntity>>(emptyList())
    val queue: StateFlow<List<SongEntity>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _remixState = MutableStateFlow(RemixState())
    val remixState: StateFlow<RemixState> = _remixState.asStateFlow()

    private val _visualizerFrequencies = MutableStateFlow(FloatArray(32) { 0.1f })
    val visualizerFrequencies: StateFlow<FloatArray> = _visualizerFrequencies.asStateFlow()

    private val _visualizerType = MutableStateFlow(VisualizerType.BARS)
    val visualizerType: StateFlow<VisualizerType> = _visualizerType.asStateFlow()

    // Full screen player & remix sheet expansion states
    private val _isFullScreenPlayerOpen = MutableStateFlow(false)
    val isFullScreenPlayerOpen: StateFlow<Boolean> = _isFullScreenPlayerOpen.asStateFlow()

    private val _isRemixStudioOpen = MutableStateFlow(false)
    val isRemixStudioOpen: StateFlow<Boolean> = _isRemixStudioOpen.asStateFlow()

    private var progressJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        startProgressTracking()
        startVisualizerEngine()
    }

    fun openFullScreenPlayer(open: Boolean) {
        _isFullScreenPlayerOpen.value = open
    }

    fun openRemixStudio(open: Boolean) {
        _isRemixStudioOpen.value = open
    }

    fun setVisualizerType(type: VisualizerType) {
        _visualizerType.value = type
    }

    fun playSong(song: SongEntity, newQueue: List<SongEntity> = listOf(song)) {
        val index = newQueue.indexOfFirst {
            (song.id != 0L && it.id == song.id) ||
            (!song.externalId.isNullOrBlank() && it.externalId == song.externalId) ||
            (it.uriString.isNotBlank() && it.uriString == song.uriString)
        }.let { if (it >= 0) it else 0 }
        _queue.value = if (newQueue.isNotEmpty()) newQueue else listOf(song)
        _currentIndex.value = index
        loadAndPlay(song)
    }

    private fun loadAndPlay(song: SongEntity) {
        scope.launch(Dispatchers.Main) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null

                val player = MediaPlayer()
                val uri = Uri.parse(song.uriString)

                if (song.uriString.startsWith("http://") || song.uriString.startsWith("https://")) {
                    player.setDataSource(song.uriString)
                } else if (song.uriString.startsWith("content://") || song.uriString.startsWith("file://")) {
                    player.setDataSource(context, uri)
                } else {
                    val file = File(song.uriString)
                    if (file.exists()) {
                        player.setDataSource(file.absolutePath)
                    } else {
                        // Fallback or generate audio file if procedural track
                        val generated = generateSampleMelodyFile(song.title)
                        player.setDataSource(generated.absolutePath)
                    }
                }

                player.setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong()
                    _currentSong.value = song
                    _isPlaying.value = true

                    // Apply Remix Studio effects only to local or permitted audio
                    if (!song.isOnline) {
                        effectsManager.attachToSession(mp.audioSessionId, mp, _remixState.value)
                    }
                    mp.start()
                }

                player.setOnCompletionListener {
                    onSongCompleted()
                }

                player.setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerManager", "MediaPlayer error what=$what extra=$extra")
                    _isPlaying.value = false
                    true
                }

                player.prepareAsync()
                mediaPlayer = player

            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Failed to play song: ${e.message}", e)
                _isPlaying.value = false
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                effectsManager.applyAll(player, _remixState.value)
                _isPlaying.value = true
            }
        } ?: run {
            _currentSong.value?.let { loadAndPlay(it) }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Stop error: ${e.message}")
        }
        _isPlaying.value = false
        _currentSong.value = null
        _currentPositionMs.value = 0L
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Seek error: ${e.message}")
        }
    }

    fun seekRelative(seconds: Int) {
        mediaPlayer?.let { player ->
            val newPos = (player.currentPosition + (seconds * 1000)).coerceIn(0, player.duration)
            seekTo(newPos.toLong())
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffle.value) {
            Random.nextInt(q.size)
        } else {
            (_currentIndex.value + 1) % q.size
        }
        _currentIndex.value = nextIndex
        loadAndPlay(q[nextIndex])
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If played more than 3 seconds, replay current song first
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
        _currentIndex.value = prevIndex
        loadAndPlay(q[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    private fun onSongCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                if (_currentIndex.value < q.size - 1) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    seekTo(0)
                }
            }
        }
    }

    // Remix Studio controls
    fun updateBass(bass: Float) {
        val updated = _remixState.value.copy(bass = bass.coerceIn(-100f, 100f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyBass(updated.bass)
    }

    fun updateTreble(treble: Float) {
        val updated = _remixState.value.copy(treble = treble.coerceIn(-100f, 100f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyTreble(updated.treble)
    }

    fun updateVocal(vocal: Float) {
        val updated = _remixState.value.copy(vocal = vocal.coerceIn(-100f, 100f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyVocal(updated.vocal)
    }

    fun updateReverb(reverb: Float) {
        val updated = _remixState.value.copy(reverb = reverb.coerceIn(0f, 100f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyReverb(updated.reverb)
    }

    fun updateEchoDelay(echo: Float) {
        val updated = _remixState.value.copy(echoDelay = echo.coerceIn(0f, 100f), activePresetName = null)
        _remixState.value = updated
        // Echo/Delay influences reverb depth + feedback simulation
        effectsManager.applyReverb((updated.reverb * 0.7f + echo * 0.3f).coerceIn(0f, 100f))
    }

    fun updateSpeed(speed: Float) {
        val updated = _remixState.value.copy(speed = speed.coerceIn(0.5f, 2.0f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyPlaybackParams(mediaPlayer, updated.speed, updated.pitch)
    }

    fun updatePitch(pitch: Float) {
        val updated = _remixState.value.copy(pitch = pitch.coerceIn(0.5f, 2.0f), activePresetName = null)
        _remixState.value = updated
        effectsManager.applyPlaybackParams(mediaPlayer, updated.speed, updated.pitch)
    }

    fun updateVolume(vol: Float) {
        val updated = _remixState.value.copy(volume = vol.coerceIn(0f, 100f))
        _remixState.value = updated
        effectsManager.applyVolumeAndBalance(mediaPlayer, updated.volume, updated.balance)
    }

    fun updateBalance(balance: Float) {
        val updated = _remixState.value.copy(balance = balance.coerceIn(-100f, 100f))
        _remixState.value = updated
        effectsManager.applyVolumeAndBalance(mediaPlayer, updated.volume, updated.balance)
    }

    fun applyPreset(presetName: String) {
        val state = when (presetName) {
            "Normal" -> RemixState(activePresetName = "Normal")
            "Bass Boost" -> RemixState(bass = 68f, treble = 15f, vocal = 0f, reverb = 15f, speed = 1.0f, pitch = 1.0f, activePresetName = "Bass Boost")
            "Night" -> RemixState(bass = 30f, treble = -20f, vocal = 10f, reverb = 45f, speed = 0.92f, pitch = 0.95f, activePresetName = "Night")
            "Dreamy" -> RemixState(bass = 20f, treble = 35f, vocal = 15f, reverb = 75f, echoDelay = 40f, speed = 0.95f, pitch = 1.05f, activePresetName = "Dreamy")
            "Vocal" -> RemixState(bass = -15f, treble = 25f, vocal = 65f, reverb = 20f, speed = 1.0f, pitch = 1.0f, activePresetName = "Vocal")
            "Club" -> RemixState(bass = 80f, treble = 40f, vocal = 20f, reverb = 30f, speed = 1.06f, pitch = 1.0f, activePresetName = "Club")
            "Chill" -> RemixState(bass = 35f, treble = -10f, vocal = 5f, reverb = 35f, speed = 0.88f, pitch = 0.96f, activePresetName = "Chill")
            else -> RemixState(activePresetName = presetName)
        }
        _remixState.value = state
        effectsManager.applyAll(mediaPlayer, state)
    }

    fun applyCustomPreset(preset: RemixPresetEntity) {
        val state = RemixState(
            bass = preset.bass,
            treble = preset.treble,
            vocal = preset.vocal,
            reverb = preset.reverb,
            echoDelay = preset.echoDelay,
            speed = preset.speed,
            pitch = preset.pitch,
            volume = preset.volume,
            balance = preset.balance,
            activePresetName = preset.name
        )
        _remixState.value = state
        effectsManager.applyAll(mediaPlayer, state)
    }

    fun resetEffects() {
        val state = RemixState(activePresetName = "Normal")
        _remixState.value = state
        effectsManager.applyAll(mediaPlayer, state)
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                        _durationMs.value = player.duration.toLong()
                    }
                }
                delay(250)
            }
        }
    }

    private fun startVisualizerEngine() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch(Dispatchers.Default) {
            var phase = 0.0f
            while (isActive) {
                if (_isPlaying.value) {
                    val bassFactor = (1f + (_remixState.value.bass / 150f)).coerceIn(0.5f, 2f)
                    val speedFactor = _remixState.value.speed
                    phase += 0.2f * speedFactor

                    val bars = FloatArray(32) { index ->
                        val baseSine = (sin(phase + index * 0.45f) + 1f) / 2f
                        val wave = (sin(phase * 1.5f + index * 0.25f) + 1f) / 2f
                        val randomJitter = Random.nextFloat() * 0.15f
                        val height = ((baseSine * 0.6f + wave * 0.3f + randomJitter) * bassFactor).coerceIn(0.08f, 1f)
                        height
                    }
                    _visualizerFrequencies.value = bars
                } else {
                    val bars = FloatArray(32) { 0.05f }
                    _visualizerFrequencies.value = bars
                }
                delay(40)
            }
        }
    }

    fun parseAudioFile(uri: Uri): SongEntity? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Audio Track"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Local Music"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            val artBytes = retriever.embeddedPicture
            val artBase64 = if (artBytes != null && artBytes.isNotEmpty()) {
                try {
                    // Compress thumbnail for smooth database storage
                    val bmp = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                    if (bmp != null) {
                        val scaled = Bitmap.createScaledBitmap(bmp, 120, 120, true)
                        val stream = ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    } else null
                } catch (e: Exception) {
                    null
                }
            } else null

            val colors = listOf("#EC4899", "#8B5CF6", "#3B82F6", "#10B981", "#F59E0B", "#6366F1", "#14B8A6")
            val colorHex = colors[Random.nextInt(colors.size)]

            SongEntity(
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                uriString = uri.toString(),
                albumColorHex = colorHex,
                artworkBase64 = artBase64
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Parse metadata error: ${e.message}")
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Song"
            SongEntity(
                title = fileName,
                artist = "Local Device",
                album = "Music Library",
                durationMs = 0L,
                uriString = uri.toString(),
                albumColorHex = "#8B5CF6"
            )
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    /**
     * Generates a pleasant, melodic 16-bit PCM WAV audio file dynamically.
     * Guarantees Priyanka has ready-to-play relaxing ambient music even before importing files!
     */
    fun generateSampleMelodyFile(title: String): File {
        val cacheDir = File(context.cacheDir, "sample_audio")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val safeName = title.replace(Regex("[^a-zA-Z0-9]"), "_").take(24)
        val audioFile = File(cacheDir, "$safeName.wav")
        if (audioFile.exists() && audioFile.length() > 1000) {
            return audioFile
        }

        // Generate peaceful 45-second looping chord progression
        val sampleRate = 22050
        val durationSeconds = 30
        val totalSamples = sampleRate * durationSeconds

        val pcmData = ByteArray(totalSamples * 2)
        val frequencies = when {
            title.contains("Piano", true) -> listOf(261.63, 329.63, 392.00, 523.25, 440.00) // C-E-G-C-A
            title.contains("Starlight", true) -> listOf(293.66, 369.99, 440.00, 587.33, 329.63) // D-F#-A-D-E
            title.contains("Serenade", true) -> listOf(220.00, 261.63, 329.63, 392.00, 440.00) // A-C-E-G-A
            else -> listOf(261.63, 311.13, 392.00, 466.16, 523.25) // C-Eb-G-Bb-C
        }

        var bufferIdx = 0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val noteIdx = ((t / 2.0).toInt()) % frequencies.size
            val baseFreq = frequencies[noteIdx]

            // Main tone + gentle octave overtone + soft decay envelope
            val noteTime = t % 2.0
            val envelope = (1.0 - noteTime / 2.0).coerceIn(0.0, 1.0)
            val wave = (sin(2.0 * PI * baseFreq * t) * 0.6 + sin(2.0 * PI * baseFreq * 2.0 * t) * 0.3) * envelope
            val sampleVal = (wave * 20000.0).toInt().coerceIn(-32768, 32767).toShort()

            pcmData[bufferIdx++] = (sampleVal.toInt() and 0xFF).toByte()
            pcmData[bufferIdx++] = ((sampleVal.toInt() shr 8) and 0xFF).toByte()
        }

        // Write WAV header
        writeWavHeader(audioFile, sampleRate, 1, 16, pcmData.size)
        FileOutputStream(audioFile, true).use { out ->
            out.write(pcmData)
        }

        return audioFile
    }

    private fun writeWavHeader(file: File, sampleRate: Int, channels: Int, bitsPerSample: Int, pcmSize: Int) {
        val totalDataLen = pcmSize + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size for PCM
            putShort(1) // AudioFormat PCM
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(pcmSize)
        }

        FileOutputStream(file).use { out ->
            out.write(header.array())
        }
    }

    fun release() {
        progressJob?.cancel()
        visualizerJob?.cancel()
        effectsManager.releaseEffects()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
