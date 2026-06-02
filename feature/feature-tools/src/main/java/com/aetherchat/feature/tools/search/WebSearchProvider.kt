package com.aetherchat.feature.tools.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

interface WebSearchProvider {
    val id: String
    val displayName: String
    suspend fun search(query: String, maxResults: Int = 5): Result<List<SearchResult>>
    suspend fun testConnection(): Result<Unit>
}

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
)

class SearxngSearchProvider(
    private val baseUrl: String,
) : WebSearchProvider {

    override val id = "searxng"
    override val displayName = "SearXNG"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    override suspend fun search(query: String, maxResults: Int): Result<List<SearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$baseUrl/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&format=json&limit=$maxResults"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

                val body = response.body?.string() ?: throw Exception("Empty response")
                val root = json.parseToJsonElement(body).jsonObject
                val results = root["results"]?.jsonArray ?: return@runCatching emptyList()

                results.mapNotNull { element ->
                    val obj = element.jsonObject
                    SearchResult(
                        title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                        url = obj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                        snippet = obj["content"]?.jsonPrimitive?.content ?: "",
                    )
                }
            }
        }

    override suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url("$baseUrl/search?q=test&format=json&limit=1").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        }
    }
}

private val kotlinx.serialization.json.JsonElement.jsonObject get() = this as kotlinx.serialization.json.JsonObject
private val kotlinx.serialization.json.JsonElement.jsonArray get() = this as kotlinx.serialization.json.JsonArray
private val kotlinx.serialization.json.JsonElement.jsonPrimitive get() = this as kotlinx.serialization.json.JsonPrimitive
