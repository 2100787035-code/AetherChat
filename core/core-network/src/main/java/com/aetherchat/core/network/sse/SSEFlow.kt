package com.aetherchat.core.network.sse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

fun createSSEFlow(
    client: OkHttpClient,
    request: Request,
): Flow<SSEEvent> = callbackFlow {
    val eventSourceFactory = EventSources.createFactory(client)

    val eventSource = eventSourceFactory.newEventSource(request, object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            if (data == "[DONE]") {
                trySend(SSEEvent(id = id, event = type, data = "[DONE]"))
                close()
                return
            }
            trySend(SSEEvent(id = id, event = type, data = data))
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            if (t != null) {
                close(t)
            } else {
                close()
            }
        }

        override fun onClosed(eventSource: EventSource) {
            close()
        }
    })

    awaitClose {
        eventSource.cancel()
    }
}
