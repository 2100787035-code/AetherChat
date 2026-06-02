package com.aetherchat.core.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class WebDavConfig(
    val serverUrl: String,
    val username: String,
    val password: String,
    val remotePath: String = "/AetherChat/",
)

class WebDavService(
    private val config: WebDavConfig,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val authHeader: String
        get() = Credentials.basic(config.username, config.password)

    suspend fun upload(fileName: String, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath}$fileName"
                val body = content.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .put(body)
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("WebDAV upload failed: HTTP ${response.code}")
                }
            }
        }

    suspend fun download(fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath}$fileName"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("WebDAV download failed: HTTP ${response.code}")
                }
                response.body?.string() ?: throw Exception("Empty response")
            }
        }

    suspend fun listFiles(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath}"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .addHeader("Depth", "1")
                    .method("PROPFIND", null)
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful && response.code != 207) {
                    throw Exception("WebDAV list failed: HTTP ${response.code}")
                }
                val body = response.body?.string() ?: return@withContext emptyList()
                parseFileList(body)
            }
        }

    suspend fun delete(fileName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath}$fileName"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .delete()
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful && response.code != 204 && response.code != 404) {
                    throw Exception("WebDAV delete failed: HTTP ${response.code}")
                }
            }
        }

    suspend fun testConnection(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath}"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .addHeader("Depth", "0")
                    .method("PROPFIND", null)
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful && response.code != 207) {
                    throw Exception("WebDAV connection test failed: HTTP ${response.code}")
                }
            }
        }

    suspend fun ensureRemoteDirectory(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "${config.serverUrl.trimEnd('/')}${config.remotePath.trimEnd('/')}"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authHeader)
                    .method("MKCOL", null)
                    .build()
                client.newCall(request).execute()
            }
        }

    private fun parseFileList(xml: String): List<String> {
        val files = mutableListOf<String>()
        val hrefRegex = Regex("<d:href>([^<]+)</d:href>", RegexOption.IGNORE_CASE)
        hrefRegex.findAll(xml).forEach { match ->
            val href = match.groupValues[1]
            val fileName = href.substringAfterLast("/")
            if (fileName.isNotBlank() && fileName.endsWith(".json")) {
                files.add(fileName)
            }
        }
        return files
    }
}
