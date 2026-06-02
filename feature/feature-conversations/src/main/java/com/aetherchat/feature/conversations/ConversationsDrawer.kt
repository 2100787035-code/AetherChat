package com.aetherchat.feature.conversations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@Composable
fun ConversationsDrawer(
    viewModel: ConversationsViewModel,
    onConversationClick: (String) -> Unit,
    onNewConversation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "AetherChat",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                "✏️",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.clickable(onClick = onNewConversation),
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("搜索对话…") },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.InputField,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            if (uiState.pinnedConversations.isNotEmpty()) {
                item {
                    Text("📌 置顶", style = MaterialTheme.typography.titleSmall)
                }
                items(uiState.pinnedConversations, key = { it.conversation.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onConversationClick(item.conversation.id) },
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.sm)) }
            }

            if (uiState.todayConversations.isNotEmpty()) {
                item {
                    Text("今天", style = MaterialTheme.typography.titleSmall)
                }
                items(uiState.todayConversations, key = { it.conversation.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onConversationClick(item.conversation.id) },
                    )
                }
            }

            if (uiState.yesterdayConversations.isNotEmpty()) {
                item {
                    Text("昨天", style = MaterialTheme.typography.titleSmall)
                }
                items(uiState.yesterdayConversations, key = { it.conversation.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onConversationClick(item.conversation.id) },
                    )
                }
            }

            if (uiState.olderConversations.isNotEmpty()) {
                item {
                    Text("更早", style = MaterialTheme.typography.titleSmall)
                }
                items(uiState.olderConversations, key = { it.conversation.id }) { item ->
                    ConversationRow(
                        item = item,
                        onClick = { onConversationClick(item.conversation.id) },
                    )
                }
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToSettings)
                .padding(vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⚙️", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.padding(horizontal = AppSpacing.sm))
            Text("设置", style = MaterialTheme.typography.bodyLarge)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToAbout)
                .padding(vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("👤", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.padding(horizontal = AppSpacing.sm))
            Text("关于", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ConversationRow(
    item: ConversationItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShape.Card,
        tonalElevation = androidx.compose.material3.CardDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        ) {
            Text(
                text = item.conversation.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.lastMessagePreview.isNotBlank()) {
                Text(
                    text = item.lastMessagePreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
