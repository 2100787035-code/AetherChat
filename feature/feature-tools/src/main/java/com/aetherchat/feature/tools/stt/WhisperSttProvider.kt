package com.aetherchat.feature.tools.stt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

interface SttProvider {
    val id: String
    val displayName: String
    suspend fun transcribe(audioFile: File, language: String? = null): Result<String>
    suspend fun testConnection(): Result<Unit>
}

class WhisperSttProvider(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1",
    private val model: String = "whisper-1",
) : SttProvider {

    override val id = "openai_whisper"
    override val displayName = "OpenAI Whisper"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun transcribe(audioFile: File, language: String?): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", audioFile.name,
                        audioFile.asRequestBody("audio/*".toMediaType()))
                    .addFormDataPart("model", model)
                    .apply {
                        language?.let { addFormDataPart("language", it) }
                    }
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl/audio/transcriptions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Whisper API error: HTTP ${response.code}")
                }

                val body = response.body?.string() ?: throw Exception("Empty response")
                val root = json.parseToJsonElement(body).jsonObject
                root["text"]?.jsonPrimitive?.contentOrNull ?: throw Exception("No text in response")
            }
        }

    override suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        }
    }
}
