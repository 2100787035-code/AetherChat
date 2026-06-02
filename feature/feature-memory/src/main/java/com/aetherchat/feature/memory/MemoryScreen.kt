package com.aetherchat.feature.memory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记忆与知识库") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.md),
        ) {
            Text("搜索记忆", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = { Text("搜索记忆…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text("知识库", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            if (uiState.knowledgeBases.isEmpty()) {
                Text(
                    "暂无知识库，点击下方创建",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    items(uiState.knowledgeBases, key = { it.id }) { kb ->
                        KnowledgeBaseCard(
                            kb = kb,
                            onClick = { viewModel.selectKnowledgeBase(kb) },
                            onDelete = { viewModel.deleteKnowledgeBase(kb.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text("记忆条目", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            if (uiState.memories.isEmpty()) {
                Text(
                    "对话中提取的记忆将显示在这里",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    items(uiState.memories, key = { it.id }) { memory ->
                        MemoryCard(
                            memory = memory,
                            onDelete = { viewModel.deleteMemory(memory.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeBaseCard(
    kb: KnowledgeBase,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(kb.name, style = MaterialTheme.typography.titleMedium)
                if (kb.description.isNotBlank()) {
                    Text(
                        kb.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "${kb.documentCount} 文档 · ${kb.totalChunks} 分块",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("🗑️", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable(onClick = onDelete))
        }
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryEntry,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    memory.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    memory.source.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("🗑️", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable(onClick = onDelete))
        }
    }
}
