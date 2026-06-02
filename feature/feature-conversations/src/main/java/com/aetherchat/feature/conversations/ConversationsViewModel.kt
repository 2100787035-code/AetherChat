package com.aetherchat.feature.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.ConversationEntity
import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class ConversationsViewModel(
    private val database: AetherChatDatabase,
) : ViewModel() {

    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            searchQueryFlow
                .flatMapLatest { query ->
                    if (query.isBlank()) conversationDao.getAll()
                    else conversationDao.search(query)
                }
                .collect { entities ->
                    val items = entities.map { entity ->
                        val messages = messageDao.getByConversationId(entity.id).first()
                        val lastMessage = messages.lastOrNull()
                        val preview = lastMessage?.content
                            ?.filterIsInstance<ContentBlock.Text>()
                            ?.firstOrNull()?.text ?: ""
                        val lastTime = lastMessage?.createdAt ?: entity.updatedAt
                        ConversationItem(
                            conversation = entity.toDomain(),
                            lastMessagePreview = preview,
                            lastMessageTime = lastTime,
                        )
                    }
                    groupAndUpdate(items)
                }
        }
    }

    private fun groupAndUpdate(items: List<ConversationItem>) {
        val pinned = items.filter { it.conversation.isPinned }
        val nonPinned = items.filter { !it.conversation.isPinned }

        val startOfToday = startOfDay(System.currentTimeMillis())
        val startOfYesterday = startOfToday - 24 * 60 * 60 * 1000L

        val today = nonPinned.filter { item ->
            val time = if (item.lastMessageTime > 0) item.lastMessageTime else item.conversation.updatedAt
            time >= startOfToday
        }
        val yesterday = nonPinned.filter { item ->
            val time = if (item.lastMessageTime > 0) item.lastMessageTime else item.conversation.updatedAt
            time >= startOfYesterday && time < startOfToday
        }
        val older = nonPinned.filter { item ->
            val time = if (item.lastMessageTime > 0) item.lastMessageTime else item.conversation.updatedAt
            time < startOfYesterday
        }

        _uiState.update {
            it.copy(
                pinnedConversations = pinned,
                todayConversations = today,
                yesterdayConversations = yesterday,
                olderConversations = older,
                isRefreshing = false,
            )
        }
    }

    private fun startOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun createNewConversation(): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            conversationDao.insert(
                ConversationEntity(
                    id = id,
                    title = "新对话",
                    assistantId = null,
                    providerId = "",
                    modelId = "",
                    systemPrompt = null,
                    tags = emptyList(),
                    isPinned = false,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
        return id
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            val entity = conversationDao.getById(id) ?: return@launch
            conversationDao.delete(entity)
        }
    }

    fun togglePin(id: String) {
        viewModelScope.launch {
            val entity = conversationDao.getById(id) ?: return@launch
            conversationDao.update(entity.copy(isPinned = !entity.isPinned))
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            val entity = conversationDao.getById(id) ?: return@launch
            conversationDao.update(entity.copy(title = newTitle))
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    assistantId = assistantId,
    providerId = providerId,
    modelId = modelId,
    systemPrompt = systemPrompt,
    tags = tags,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
