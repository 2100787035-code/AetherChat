package com.aetherchat.feature.memory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MemoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private val _createKbState = MutableStateFlow(CreateKnowledgeBaseUiState())
    val createKbState: StateFlow<CreateKnowledgeBaseUiState> = _createKbState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchMemories() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return
        _uiState.update { it.copy(isSearching = true) }
    }

    fun selectKnowledgeBase(kb: KnowledgeBase?) {
        _uiState.update { it.copy(selectedKnowledgeBase = kb) }
    }

    fun deleteMemory(id: String) {
        _uiState.update { state ->
            state.copy(memories = state.memories.filter { it.id != id })
        }
    }

    fun deleteKnowledgeBase(id: String) {
        _uiState.update { state ->
            state.copy(knowledgeBases = state.knowledgeBases.filter { it.id != id })
        }
    }

    fun updateCreateKbName(name: String) {
        _createKbState.update { it.copy(name = name) }
    }

    fun updateCreateKbDescription(desc: String) {
        _createKbState.update { it.copy(description = desc) }
    }

    fun createKnowledgeBase(): KnowledgeBase? {
        val state = _createKbState.value
        if (state.name.isBlank()) return null
        val now = System.currentTimeMillis()
        val kb = KnowledgeBase(
            id = java.util.UUID.randomUUID().toString(),
            name = state.name,
            description = state.description,
            createdAt = now,
            updatedAt = now,
        )
        _uiState.update { it.copy(knowledgeBases = it.knowledgeBases + kb) }
        _createKbState.update { CreateKnowledgeBaseUiState() }
        return kb
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
