package com.aetherchat.core.network.sse

object SSEParser {

    fun parse(chunk: String): List<SSEEvent> {
        val events = mutableListOf<SSEEvent>()
        val eventChunks = chunk.split("\n\n")

        for (eventChunk in eventChunks) {
            if (eventChunk.isBlank()) continue

            var id: String? = null
            var event: String? = null
            val dataBuilder = StringBuilder()
            var retry: Int? = null

            for (line in eventChunk.split("\n")) {
                when {
                    line.startsWith("id:") -> id = line.removePrefix("id:").trim()
                    line.startsWith("event:") -> event = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> {
                        if (dataBuilder.isNotEmpty()) dataBuilder.append("\n")
                        dataBuilder.append(line.removePrefix("data:").trim())
                    }
                    line.startsWith("retry:") -> retry = line.removePrefix("retry:").trim().toIntOrNull()
                }
            }

            if (dataBuilder.isNotEmpty()) {
                events.add(SSEEvent(id = id, event = event, data = dataBuilder.toString(), retry = retry))
            }
        }

        return events
    }
}
