package com.aetherchat.feature.tools.stt

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

class SttManager(
    private val context: Context,
    private val provider: SttProvider,
) {
    private var recorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false

    fun startRecording(): File {
        val file = File(context.cacheDir, "recording_${UUID.randomUUID()}.m4a")
        currentRecordingFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        isRecording = true
        return file
    }

    fun stopRecording(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            currentRecordingFile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun transcribeRecording(language: String? = null): Result<String> {
        val file = currentRecordingFile ?: return Result.failure(Exception("No recording"))
        return provider.transcribe(file, language)
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    fun release() {
        try {
            recorder?.apply {
                if (isRecording) stop()
                release()
            }
        } catch (_: Exception) {
        }
        recorder = null
        currentRecordingFile = null
        isRecording = false
    }
}
