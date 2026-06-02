package com.aetherchat.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface TTSProvider {
    val id: String
    val displayName: String

    suspend fun listVoices(): Result<List<VoiceInfo>>
    fun synthesizeStream(text: String, config: TTSConfig): Flow<ByteArray>
    suspend fun testConnection(): Result<Unit>
}

@Serializable
data class TTSConfig(
    val apiKey: String,
    val baseUrl: String? = null,
    val voice: String,
    val speed: Float = 1.0f,
    val outputFormat: AudioFormat = AudioFormat.MP3,
)

@Serializable
enum class AudioFormat {
    MP3, WAV, OGG, PCM
}

@Serializable
data class VoiceInfo(
    val id: String,
    val name: String,
    val language: String?,
    val gender: String?,
)
