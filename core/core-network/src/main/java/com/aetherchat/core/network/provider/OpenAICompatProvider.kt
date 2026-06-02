package com.aetherchat.core.network.provider

import com.aetherchat.core.network.api.ChatCompletionRequest
import com.aetherchat.core.network.api.ChatMessageDto
import com.aetherchat.core.network.api.ModelDto
import com.aetherchat.core.network.api.ModelsResponse
import com.aetherchat.core.network.sse.SSEEvent
import com.aetherchat.core.network.sse.createSSEFlow
import com.aetherchat.domain.model.ChatRequest
import com.aetherchat.domain.model.ChatResponse
import com.aetherchat.domain.model.ChatStreamEvent
import com.aetherchat.domain.model.ConnectionInfo
import com.aetherchat.domain.model.LLMProvider
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.ModelTestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAICompatProvider(
    override val id: String,
    override val displayName: String,
    private val baseUrl: String,
    private val apiKey: String,
    private val client: OkHttpClient = defaultClient(),
) : LLMProvider {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun chatStream(request: ChatRequest): Flow<ChatStreamEvent> {
        val requestBody = json.encodeToString(
            ChatCompletionRequest.serializer(),
            request.toApiRequest()
        )

        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return createSSEFlow(client, httpRequest).map { event ->
            parseStreamEvent(event)
        }
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val apiRequest = request.toApiRequest().copy(stream = false)
        val requestBody = json.encodeToString(
            ChatCompletionRequest.serializer(),
            apiRequest
        )

        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(httpRequest).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val apiResponse = json.decodeFromString(
            com.aetherchat.core.network.api.ChatCompletionResponse.serializer(),
            body
        )

        val choice = apiResponse.choices.firstOrNull()
        return ChatResponse(
            message = com.aetherchat.domain.model.ChatMessage(
                role = com.aetherchat.domain.model.Role.ASSISTANT,
                content = choice?.message?.content ?: "",
            ),
            inputTokens = apiResponse.usage?.prompt_tokens,
            outputTokens = apiResponse.usage?.completion_tokens,
        )
    }

    override suspend fun listModels(): Result<List<ModelInfo>> = runCatching {
        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/models")
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

        val response = client.newCall(httpRequest).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val modelsResponse = json.decodeFromString(ModelsResponse.serializer(), body)

        modelsResponse.data.map { it.toModelInfo(id) }
    }

    override suspend fun testConnection(): Result<ConnectionInfo> = runCatching {
        val startTime = System.currentTimeMillis()
        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/models")
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

        val response = client.newCall(httpRequest).execute()
        val latency = System.currentTimeMillis() - startTime

        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val body = response.body?.string() ?: throw Exception("Empty response")
        val modelsResponse = json.decodeFromString(ModelsResponse.serializer(), body)

        ConnectionInfo(latencyMs = latency, availableModelCount = modelsResponse.data.size)
    }

    override suspend fun testModel(modelId: String): Result<ModelTestResult> = runCatching {
        val startTime = System.currentTimeMillis()
        val testRequest = ChatCompletionRequest(
            model = modelId,
            messages = listOf(ChatMessageDto(role = "user", content = "Hi")),
            max_tokens = 5,
            stream = false,
        )
        val requestBody = json.encodeToString(
            ChatCompletionRequest.serializer(),
            testRequest
        )

        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(httpRequest).execute()
        val latency = System.currentTimeMillis() - startTime

        ModelTestResult(
            modelId = modelId,
            success = response.isSuccessful,
            latencyMs = latency,
            errorMessage = if (response.isSuccessful) null else "HTTP ${response.code}",
        )
    }

    private fun parseStreamEvent(event: SSEEvent): ChatStreamEvent {
        if (event.data == "[DONE]") return ChatStreamEvent.Done

        return try {
            val response = json.decodeFromString(
                com.aetherchat.core.network.api.ChatCompletionResponse.serializer(),
                event.data
            )
            val choice = response.choices.firstOrNull()
            val delta = choice?.delta

            when {
                delta?.tool_calls != null -> {
                    val toolCall = delta.tool_calls.first()
                    when {
                        toolCall.id != null -> ChatStreamEvent.ToolCallStart(
                            id = toolCall.id,
                            name = toolCall.function?.name ?: "",
                        )
                        toolCall.function?.arguments != null -> ChatStreamEvent.ToolCallDelta(
                            inputDelta = toolCall.function.arguments,
                        )
                        else -> ChatStreamEvent.Token(text = "")
                    }
                }
                delta?.content != null -> ChatStreamEvent.Token(text = delta.content)
                response.usage != null -> ChatStreamEvent.Usage(
                    inputTokens = response.usage?.prompt_tokens ?: 0,
                    outputTokens = response.usage?.completion_tokens ?: 0,
                )
                else -> ChatStreamEvent.Token(text = "")
            }
        } catch (e: Exception) {
            ChatStreamEvent.Error(errorMessage = e.message ?: "Parse error")
        }
    }

    private fun ChatRequest.toApiRequest() = ChatCompletionRequest(
        model = modelId,
        messages = messages.map { ChatMessageDto(role = it.role.name.lowercase(), content = it.content) },
        temperature = temperature,
        max_tokens = maxTokens,
        stream = stream,
    )

    private fun ModelDto.toModelInfo(providerId: String) = ModelInfo(
        id = id,
        providerId = providerId,
        displayName = id,
    )
}
