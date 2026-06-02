package com.aetherchat.feature.tools.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class McpClient(
    private val config: McpServerConfig,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var requestId = 0
    private var connectedTools: List<McpTool> = emptyList()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun connect(): Result<List<McpTool>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = config.url ?: throw Exception("No URL configured for MCP server")
            val response = sendRequest(url, "tools/list", null)
            val toolsArray = response.result?.let { parseTools(it) } ?: emptyList()
            connectedTools = toolsArray
            toolsArray
        }
    }

    suspend fun callTool(name: String, arguments: Map<String, String>): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = config.url ?: throw Exception("No URL configured")
                val params = buildJsonObject {
                    put("name", JsonPrimitive(name))
                    put("arguments", buildJsonObject {
                        arguments.forEach { (key, value) ->
                            put(key, JsonPrimitive(value))
                        }
                    })
                }
                val response = sendRequest(url, "tools/call", params)
                response.result?.toString() ?: ""
            }
        }

    private fun sendRequest(url: String, method: String, params: JsonObject?): McpResponse {
        val request = McpRequest(
            id = ++requestId,
            method = method,
            params = params?.let { mapOf(it.keys.first() to it.values.first()) },
        )

        val body = json.encodeToString(McpRequest.serializer(), request.copy(params = params?.let {
            val map = mutableMapOf<String, JsonElement>()
            it.entries.forEach { (k, v) -> map[k] = v }
            map
        }))

        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val httpResponse = client.newCall(httpRequest).execute()
        val responseBody = httpResponse.body?.string() ?: throw Exception("Empty response")

        return json.decodeFromString(McpResponse.serializer(), responseBody)
    }

    private fun parseTools(element: JsonElement): List<McpTool> {
        return try {
            val obj = element as? JsonObject ?: return emptyList()
            val toolsArray = obj["tools"] ?: return emptyList()
            (toolsArray as? kotlinx.serialization.json.JsonArray)?.mapNotNull { toolElement ->
                val toolObj = toolElement as? JsonObject ?: return@mapNotNull null
                McpTool(
                    name = (toolObj["name"] as? JsonPrimitive)?.content ?: return@mapNotNull null,
                    description = (toolObj["description"] as? JsonPrimitive)?.content ?: "",
                    inputSchema = toolObj["inputSchema"] ?: buildJsonObject {},
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getConnectedTools(): List<McpTool> = connectedTools

    fun disconnect() {
        connectedTools = emptyList()
        requestId = 0
    }
}
