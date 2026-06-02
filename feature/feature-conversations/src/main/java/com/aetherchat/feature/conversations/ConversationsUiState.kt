package com.aetherchat.feature.conversations

import com.aetherchat.domain.model.Conversation

data class ConversationsUiState(
    val pinnedConversations: List<ConversationItem> = emptyList(),
    val todayConversations: List<ConversationItem> = emptyList(),
    val yesterdayConversations: List<ConversationItem> = emptyList(),
    val olderConversations: List<ConversationItem> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<ConversationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class ConversationItem(
    val conversation: Conversation,
    val lastMessagePreview: String = "",
)
