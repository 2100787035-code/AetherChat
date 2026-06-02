package com.aetherchat.feature.conversations

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationsDrawer(
    viewModel: ConversationsViewModel,
    onConversationClick: (String) -> Unit,
    onNewConversation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var renameTargetId by remember { mutableStateOf("") }

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

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.weight(1f),
        ) {
            val hasConversations = uiState.pinnedConversations.isNotEmpty() ||
                uiState.todayConversations.isNotEmpty() ||
                uiState.yesterdayConversations.isNotEmpty() ||
                uiState.olderConversations.isNotEmpty()

            if (!hasConversations) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (uiState.searchQuery.isNotBlank()) "未找到对话" else "暂无对话",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (uiState.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            Text(
                                "点击 ✏️ 开始新对话",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    if (uiState.pinnedConversations.isNotEmpty()) {
                        item {
                            Text("📌 置顶", style = MaterialTheme.typography.titleSmall)
                        }
                        items(uiState.pinnedConversations, key = { it.conversation.id }) { item ->
                            SwipeableConversationRow(
                                item = item,
                                onClick = { onConversationClick(item.conversation.id) },
                                onPinToggle = { viewModel.togglePin(item.conversation.id) },
                                onDelete = { viewModel.deleteConversation(item.conversation.id) },
                                onRename = {
                                    renameTargetId = item.conversation.id
                                    renameText = item.conversation.title
                                    showRenameDialog = true
                                },
                            )
                        }
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.sm)) }
                    }

                    if (uiState.todayConversations.isNotEmpty()) {
                        item {
                            Text("今天", style = MaterialTheme.typography.titleSmall)
                        }
                        items(uiState.todayConversations, key = { it.conversation.id }) { item ->
                            SwipeableConversationRow(
                                item = item,
                                onClick = { onConversationClick(item.conversation.id) },
                                onPinToggle = { viewModel.togglePin(item.conversation.id) },
                                onDelete = { viewModel.deleteConversation(item.conversation.id) },
                                onRename = {
                                    renameTargetId = item.conversation.id
                                    renameText = item.conversation.title
                                    showRenameDialog = true
                                },
                            )
                        }
                    }

                    if (uiState.yesterdayConversations.isNotEmpty()) {
                        item {
                            Text("昨天", style = MaterialTheme.typography.titleSmall)
                        }
                        items(uiState.yesterdayConversations, key = { it.conversation.id }) { item ->
                            SwipeableConversationRow(
                                item = item,
                                onClick = { onConversationClick(item.conversation.id) },
                                onPinToggle = { viewModel.togglePin(item.conversation.id) },
                                onDelete = { viewModel.deleteConversation(item.conversation.id) },
                                onRename = {
                                    renameTargetId = item.conversation.id
                                    renameText = item.conversation.title
                                    showRenameDialog = true
                                },
                            )
                        }
                    }

                    if (uiState.olderConversations.isNotEmpty()) {
                        item {
                            Text("更早", style = MaterialTheme.typography.titleSmall)
                        }
                        items(uiState.olderConversations, key = { it.conversation.id }) { item ->
                            SwipeableConversationRow(
                                item = item,
                                onClick = { onConversationClick(item.conversation.id) },
                                onPinToggle = { viewModel.togglePin(item.conversation.id) },
                                onDelete = { viewModel.deleteConversation(item.conversation.id) },
                                onRename = {
                                    renameTargetId = item.conversation.id
                                    renameText = item.conversation.title
                                    showRenameDialog = true
                                },
                            )
                        }
                    }
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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("重命名对话") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(renameTargetId, renameText)
                        showRenameDialog = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableConversationRow(
    item: ConversationItem,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    var showContextMenu by remember { mutableStateOf(false) }

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "dismiss-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, AppShape.Card)
                    .padding(horizontal = AppSpacing.md),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ConversationRow(
                item = item,
                onClick = onClick,
                onLongClick = { showContextMenu = true },
            )
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text(if (item.conversation.isPinned) "取消置顶" else "置顶") },
                    onClick = {
                        onPinToggle()
                        showContextMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("重命名") },
                    onClick = {
                        onRename()
                        showContextMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        onDelete()
                        showContextMenu = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    item: ConversationItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick(); true },
            ),
        shape = AppShape.Card,
        tonalElevation = 2.dp,
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
