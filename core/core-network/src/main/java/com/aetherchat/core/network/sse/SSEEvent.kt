package com.aetherchat.core.network.sse

import kotlinx.serialization.Serializable

@Serializable
data class SSEEvent(
    val id: String? = null,
    val event: String? = null,
    val data: String,
    val retry: Int? = null,
)
