package com.aetherchat.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.init(conversationId)
    }

    LaunchedEffect(uiState.messages.size, uiState.streamingText) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.modelId.ifBlank { "新对话" },
                        maxLines = 1,
                    )
                },
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { }) {
                            Text("🔊")
                        }
                    }
                    IconButton(onClick = { }) {
                        Text("⋮")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = AppSpacing.md,
                    vertical = AppSpacing.sm,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.messageDiffRole),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        item = message,
                        streamingText = if (message.isStreaming) uiState.streamingText else null,
                    )
                }
            }

            ChatInputBar(
                text = uiState.inputText,
                onTextChange = viewModel::updateInput,
                onSend = viewModel::sendMessage,
                isGenerating = uiState.isGenerating,
                onStop = viewModel::stopGeneration,
            )
        }
    }
}

@Composable
private fun MessageBubble(
    item: MessageItem,
    streamingText: String?,
) {
    val isUser = item.role == MessageRole.USER

    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                shape = AppShape.UserBubble,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxMaxWidth(AppSpacing.maxBubbleWidthFraction),
            ) {
                Text(
                    text = item.content,
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("🤖", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            val displayText = streamingText ?: item.content
            Text(
                text = if (item.isStreaming && streamingText != null) "$displayText▌" else displayText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = AppSpacing.md),
            )

            if (!item.isStreaming && item.content.isNotBlank()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier.padding(top = AppSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        Text("复制", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("朗读 ▶", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("重试 ↺", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    onStop: () -> Unit,
) {
    Surface(
        tonalElevation = androidx.compose.material3.CardDefaults.cardElevation(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
        ) {
            if (isGenerating) {
                androidx.compose.material3.Button(
                    onClick = onStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.sm),
                    shape = AppShape.Button,
                ) {
                    Text("■ 停止生成")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("发消息…") },
                        shape = AppShape.InputField,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    if (text.isNotBlank()) {
                        IconButton(onClick = onSend) {
                            Text("➤", style = MaterialTheme.typography.headlineMedium)
                        }
                    } else {
                        Text("🎙️", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text("📷", style = MaterialTheme.typography.bodyLarge)
                    Text("🔍", style = MaterialTheme.typography.bodyLarge)
                    Text("🧠", style = MaterialTheme.typography.bodyLarge)
                    Text("⚡", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private fun Modifier.fillMaxMaxWidth(fraction: Float): Modifier = this.fillMaxWidth(fraction)
