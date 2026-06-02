package com.aetherchat.feature.assistants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantDetailScreen(
    viewModel: AssistantDetailViewModel,
    assistantId: String,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEmojiPicker by remember { mutableStateOf(false) }

    LaunchedEffect(assistantId) {
        viewModel.loadAssistant(assistantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${uiState.assistant?.iconEmoji ?: "🤖"} ${uiState.assistant?.name ?: "助手详情"}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                actions = {
                    Text(
                        "🗑️",
                        modifier = Modifier
                            .clickable {
                                viewModel.deleteAssistant()
                                onBack()
                            }
                            .padding(AppSpacing.md),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.isLoading) {
                Text("加载中…", style = MaterialTheme.typography.bodyMedium)
            } else if (uiState.assistant == null) {
                Text("助手不存在", style = MaterialTheme.typography.bodyMedium)
            } else {
                // 图标选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        uiState.iconEmoji,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier
                            .clickable { showEmojiPicker = true }
                            .padding(AppSpacing.sm),
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    Text("点击更换图标", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // 名称
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // 系统提示词
                OutlinedTextField(
                    value = uiState.systemPrompt,
                    onValueChange = viewModel::updateSystemPrompt,
                    label = { Text("系统提示词") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // 提供商选择
                OutlinedTextField(
                    value = uiState.selectedProviderName ?: "未选择",
                    onValueChange = {},
                    label = { Text("默认提供商") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showProviderPicker() },
                    readOnly = true,
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // 模型选择
                OutlinedTextField(
                    value = uiState.selectedModelName ?: "未选择",
                    onValueChange = {},
                    label = { Text("默认模型") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showModelPicker() },
                    readOnly = true,
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Temperature 滑块
                Text("Temperature: ${String.format("%.2f", uiState.temperature)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = uiState.temperature,
                    onValueChange = viewModel::updateTemperature,
                    valueRange = 0f..2f,
                    steps = 20,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // 保存按钮
                Button(
                    onClick = {
                        viewModel.saveAssistant()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && uiState.name.isNotBlank(),
                    shape = AppShape.Button,
                ) {
                    Text(if (uiState.isSaving) "保存中…" else "保存修改")
                }

                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // 提供商选择对话框
        if (uiState.showProviderPicker) {
            ProviderPickerDialog(
                providers = uiState.availableProviders,
                selectedId = uiState.providerId,
                onSelect = { id, name ->
                    viewModel.selectProvider(id, name)
                },
                onDismiss = viewModel::hideProviderPicker,
            )
        }

        // 模型选择对话框
        if (uiState.showModelPicker) {
            ModelPickerDialog(
                models = uiState.availableModels,
                selectedId = uiState.modelId,
                onSelect = { id, name ->
                    viewModel.selectModel(id, name)
                },
                onDismiss = viewModel::hideModelPicker,
            )
        }

        // Emoji 选择对话框
        if (showEmojiPicker) {
            EmojiPickerDialog(
                onSelect = { emoji ->
                    viewModel.updateIconEmoji(emoji)
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false },
            )
        }
    }
}

@Composable
private fun ProviderPickerDialog(
    providers: List<ProviderOption>,
    selectedId: String?,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择提供商") },
        text = {
            Column {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(provider.id, provider.name) }
                            .padding(vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (provider.id == selectedId) "✓ " else "   ",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(provider.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (providers.isEmpty()) {
                    Text("暂无可用提供商，请先添加", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun ModelPickerDialog(
    models: List<ModelOption>,
    selectedId: String?,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择模型") },
        text = {
            Column {
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(model.id, model.displayName) }
                            .padding(vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (model.id == selectedId) "✓ " else "   ",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Column {
                            Text(model.displayName, style = MaterialTheme.typography.bodyMedium)
                            if (model.contextWindow != null) {
                                Text(
                                    "${model.contextWindow / 1000}k",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (models.isEmpty()) {
                    Text("请先选择提供商", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun EmojiPickerDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val emojis = listOf("🤖", "✍️", "💻", "🌐", "📚", "🎨", "🔬", "📊", "🎵", "🎮", "🍳", "🏥", "⚖️", "🚀", "💡", "🎯")
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            Column {
                emojis.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { emoji ->
                            Text(
                                emoji,
                                style = MaterialTheme.typography.displayMedium,
                                modifier = Modifier
                                    .clickable { onSelect(emoji) }
                                    .padding(AppSpacing.sm),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

data class ProviderOption(val id: String, val name: String)
data class ModelOption(val id: String, val displayName: String, val contextWindow: Int?)