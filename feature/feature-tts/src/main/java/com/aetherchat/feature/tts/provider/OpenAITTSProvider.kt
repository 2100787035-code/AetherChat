package com.aetherchat.feature.tts.provider

import com.aetherchat.domain.model.AudioFormat
import com.aetherchat.domain.model.TTSConfig
import com.aetherchat.domain.model.TTSProvider
import com.aetherchat.domain.model.VoiceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OpenAITTSProvider(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1",
) : TTSProvider {

    override val id = "openai_tts"
    override val displayName = "OpenAI TTS"

    override suspend fun listVoices(): Result<List<VoiceInfo>> = Result.success(
        listOf(
            VoiceInfo("alloy", "Alloy", "en", null),
            VoiceInfo("echo", "Echo", "en", null),
            VoiceInfo("fable", "Fable", "en", null),
            VoiceInfo("onyx", "Onyx", "en", null),
            VoiceInfo("nova", "Nova", "en", null),
            VoiceInfo("shimmer", "Shimmer", "en", null),
        )
    )

    override fun synthesizeStream(text: String, config: TTSConfig): Flow<ByteArray> = flow {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val requestBody = kotlinx.serialization.json.buildJsonObject {
            put("model", "tts-1")
            put("input", text)
            put("voice", config.voice)
            put("speed", config.speed)
            put("response_format", config.outputFormat.name.lowercase())
        }

        val request = okhttp3.Request.Builder()
            .url("${baseUrl}/audio/speech")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.bytes() ?: return@flow
        emit(body)
    }

    override suspend fun testConnection(): Result<Unit> = runCatching {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val request = okhttp3.Request.Builder()
            .url("${baseUrl}/models")
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
    }
}

private fun String.toRequestBody(contentType: String): okhttp3.RequestBody {
    return okhttp3.RequestBody.create(contentType.toMediaType(), this.toByteArray())
}

private fun String.toMediaType(): okhttp3.MediaType =
    okhttp3.MediaType.Companion.parse(this)!!
