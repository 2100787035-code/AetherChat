package com.aetherchat.feature.conversations

import com.aetherchat.domain.model.Conversation

data class ConversationsUiState(
    val searchQuery: String = "",
    val pinnedConversations: List<ConversationItem> = emptyList(),
    val todayConversations: List<ConversationItem> = emptyList(),
    val yesterdayConversations: List<ConversationItem> = emptyList(),
    val olderConversations: List<ConversationItem> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
)

data class ConversationItem(
    val conversation: Conversation,
    val lastMessagePreview: String = "",
    val lastMessageTime: Long = 0L,
)
