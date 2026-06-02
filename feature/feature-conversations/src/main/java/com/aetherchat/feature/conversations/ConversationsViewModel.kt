package com.aetherchat.feature.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.domain.model.Conversation
import com.aetherchat.domain.model.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationsViewModel(
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun createNewConversation(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun deleteConversation(id: String) {
    }

    fun togglePin(id: String) {
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
