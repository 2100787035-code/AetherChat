package com.aetherchat.feature.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.aetherchat.domain.model.TTSConfig
import com.aetherchat.domain.model.TTSProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TtsPlayerService(
    private val context: Context,
) {
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var currentProvider: TTSProvider? = null

    enum class PlaybackState { IDLE, PLAYING, PAUSED, ERROR }

    fun play(
        provider: TTSProvider,
        text: String,
        config: TTSConfig,
    ) {
        stop()
        currentProvider = provider
        _playbackState.value = PlaybackState.PLAYING

        playbackJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                provider.synthesizeStream(text, config).collect { audioData ->
                    playAudioData(audioData)
                }
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }

    private fun playAudioData(data: ByteArray) {
        try {
            val tempFile = java.io.File.createTempFile("tts_", ".mp3", context.cacheDir)
            tempFile.writeBytes(data)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.IDLE
                    tempFile.delete()
                }
                setOnErrorListener { _, _, _ ->
                    _playbackState.value = PlaybackState.ERROR
                    tempFile.delete()
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = PlaybackState.PAUSED
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _playbackState.value = PlaybackState.PLAYING
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {
            }
            release()
        }
        mediaPlayer = null
        audioTrack?.apply {
            try {
                stop()
                release()
            } catch (_: Exception) {
            }
        }
        audioTrack = null
        _playbackState.value = PlaybackState.IDLE
    }

    fun release() {
        stop()
        currentProvider = null
    }
}
