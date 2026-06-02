package com.aetherchat.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.component.MarkdownText
import com.aetherchat.core.ui.component.StreamingCursor
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    assistantId: String? = null,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(conversationId, assistantId) {
        viewModel.loadConversation(conversationId, assistantId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.streamingText) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    if (uiState.showProviderPicker) {
        ProviderPickerDialog(
            providers = uiState.availableProviders,
            selectedId = uiState.providerId,
            onSelect = viewModel::selectProvider,
            onDismiss = viewModel::hideProviderPicker,
        )
    }

    if (uiState.showModelPicker) {
        ModelPickerDialog(
            models = uiState.availableModels,
            selectedId = uiState.modelId,
            onSelect = viewModel::selectModel,
            onDismiss = viewModel::hideModelPicker,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            uiState.title.ifBlank { "新对话" },
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            TextButton(onClick = viewModel::showProviderPicker) {
                                Text(
                                    uiState.availableProviders
                                        .find { it.id == uiState.providerId }?.name
                                        ?: "选择提供商",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            TextButton(onClick = viewModel::showModelPicker) {
                                Text(
                                    uiState.availableModels
                                        .find { it.id == uiState.modelId }?.displayName
                                        ?: "选择模型",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
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
                contentPadding = PaddingValues(
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
                onTextChange = viewModel::updateInputText,
                onSend = viewModel::sendMessage,
                isGenerating = uiState.isGenerating,
                onStop = viewModel::stopGeneration,
                webSearchEnabled = uiState.webSearchEnabled,
                onToggleWebSearch = viewModel::toggleWebSearch,
                sttEnabled = uiState.sttEnabled,
                sttIsListening = uiState.sttIsListening,
                onToggleStt = viewModel::toggleStt,
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
                modifier = Modifier.fillMaxWidth(AppSpacing.maxBubbleWidthFraction),
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
            if (displayText.isNotBlank()) {
                MarkdownText(
                    text = displayText,
                    modifier = Modifier.padding(end = AppSpacing.md),
                )
            }
            if (item.isStreaming) {
                StreamingCursor()
            }

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
                        Text(
                            "复制",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "朗读 ▶",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "重试 ↺",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
    webSearchEnabled: Boolean,
    onToggleWebSearch: () -> Unit,
    sttEnabled: Boolean,
    sttIsListening: Boolean,
    onToggleStt: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
        ) {
            if (isGenerating) {
                Button(
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
                    } else if (sttEnabled) {
                        IconButton(onClick = onToggleStt) {
                            Text(
                                if (sttIsListening) "🎙️" else "🎤",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    FilterChip(
                        selected = webSearchEnabled,
                        onClick = onToggleWebSearch,
                        label = { Text("🔍 联网搜索") },
                    )
                    FilterChip(
                        selected = sttEnabled,
                        onClick = onToggleStt,
                        label = { Text("🎙️ 语音输入") },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderPickerDialog(
    providers: List<ProviderOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择提供商") },
        text = {
            LazyColumn {
                items(providers) { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(provider.id) }
                            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = provider.id == selectedId,
                            onClick = { onSelect(provider.id) },
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text(provider.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                provider.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun ModelPickerDialog(
    models: List<ModelOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择模型") },
        text = {
            LazyColumn {
                items(models) { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(model.id) }
                            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = model.id == selectedId,
                            onClick = { onSelect(model.id) },
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text(model.displayName, style = MaterialTheme.typography.bodyLarge)
                            if (model.contextWindow != null) {
                                Text(
                                    "上下文: ${model.contextWindow}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
