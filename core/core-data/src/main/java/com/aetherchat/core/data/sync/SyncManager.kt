package com.aetherchat.core.data.sync

import com.aetherchat.core.data.export.DataExporter
import com.aetherchat.domain.model.Conversation
import com.aetherchat.domain.model.Message
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(
    private val webDavService: WebDavService,
    private val dataExporter: DataExporter,
) {
    suspend fun syncToRemote(
        providers: List<Provider>,
        models: Map<String, List<ModelInfo>>,
        conversations: List<Conversation>,
        messages: Map<String, List<Message>>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            webDavService.ensureRemoteDirectory().getOrThrow()
            val json = dataExporter.exportToJson(providers, models, conversations, messages)
            val fileName = "aetherchat_backup_${System.currentTimeMillis()}.json"
            webDavService.upload(fileName, json).getOrThrow()
        }
    }

    suspend fun syncFromRemote(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val files = webDavService.listFiles().getOrDefault(emptyList())
            if (files.isEmpty()) throw Exception("No backup files found on WebDAV server")
            val latestFile = files.maxByOrNull { it } ?: throw Exception("No files found")
            webDavService.download(latestFile).getOrThrow()
        }
    }

    suspend fun testConnection(): Result<Unit> = webDavService.testConnection()
}
