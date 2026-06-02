package com.aetherchat.feature.tools.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class McpRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String,
    val params: Map<String, JsonElement>? = null,
)

@Serializable
data class McpResponse(
    val jsonrpc: String = "2.0",
    val id: Int,
    val result: JsonElement? = null,
    val error: McpError? = null,
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonElement,
)

@Serializable
data class McpServerConfig(
    val name: String,
    val command: String? = null,
    val args: List<String>? = null,
    val url: String? = null,
    val env: Map<String, String>? = null,
)

sealed class McpConnectionState {
    data object Disconnected : McpConnectionState()
    data class Connecting(val serverName: String) : McpConnectionState()
    data class Connected(val serverName: String, val tools: List<McpTool>) : McpConnectionState()
    data class Error(val serverName: String, val message: String) : McpConnectionState()
}
