package com.aetherchat.feature.providers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    viewModel: ProviderDetailViewModel,
    providerId: String,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val modelsState by viewModel.modelsState.collectAsState()
    val addModelState by viewModel.addModelState.collectAsState()
    var showAddModelSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.provider?.name ?: "Provider")
                },
                navigationIcon = {
                    Text(
                        "←",
                        modifier = Modifier
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
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                Tab(
                    selected = uiState.selectedTab == DetailTab.BASIC,
                    onClick = { viewModel.selectTab(DetailTab.BASIC) },
                    text = { Text("基础配置") },
                )
                Tab(
                    selected = uiState.selectedTab == DetailTab.MODELS,
                    onClick = { viewModel.selectTab(DetailTab.MODELS) },
                    text = { Text("模型管理") },
                )
                Tab(
                    selected = uiState.selectedTab == DetailTab.ADVANCED,
                    onClick = { viewModel.selectTab(DetailTab.ADVANCED) },
                    text = { Text("高级") },
                )
            }

            when (uiState.selectedTab) {
                DetailTab.BASIC -> BasicConfigTab(viewModel, uiState)
                DetailTab.MODELS -> ModelsTab(
                    viewModel = viewModel,
                    state = modelsState,
                    onAddModel = { showAddModelSheet = true },
                )
                DetailTab.ADVANCED -> AdvancedConfigTab(viewModel)
            }
        }
    }

    if (showAddModelSheet) {
        AddModelBottomSheet(
            state = addModelState,
            onUpdate = viewModel::updateAddModelState,
            onAdd = {
                viewModel.addCustomModel()
                showAddModelSheet = false
            },
            onDismiss = { showAddModelSheet = false },
        )
    }
}

@Composable
private fun BasicConfigTab(
    viewModel: ProviderDetailViewModel,
    uiState: ProviderDetailUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        OutlinedTextField(
            value = "••••••••••••",
            onValueChange = {},
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            visualTransformation = if (uiState.apiKeyVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    if (uiState.apiKeyVisible) "🙈" else "👁️",
                    modifier = Modifier.padding(end = AppSpacing.sm),
                )
            },
            shape = AppShape.InputField,
        )

        Text(
            "API Key 仅存储于本地，不会上传",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs),
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        OutlinedTextField(
            value = uiState.provider?.baseUrl ?: "",
            onValueChange = {},
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            shape = AppShape.InputField,
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Button(
            onClick = viewModel::testConnection,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isTestingConnection,
            shape = AppShape.Button,
        ) {
            Text(if (uiState.isTestingConnection) "测试中…" else "🔗  测试 Provider 连接")
        }

        uiState.connectionTestResult?.let { result ->
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                text = if (result.success) "✅  连接成功  延迟 ${result.latencyMs}ms"
                else "❌  连接失败: ${result.errorMessage}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.success) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Button,
        ) {
            Text("保存配置")
        }
    }
}

@Composable
private fun ModelsTab(
    viewModel: ProviderDetailViewModel,
    state: ModelsTabUiState,
    onAddModel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        Button(
            onClick = viewModel::fetchModels,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isFetching,
            shape = AppShape.Button,
        ) {
            Text(if (state.isFetching) "检索中…" else "🔄  自动检索可用模型")
        }

        state.lastFetchTime?.let {
            Text(
                "上次检索：${formatTimestamp(it)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        if (state.autoModels.isNotEmpty()) {
            Text(
                "自动检索 (${state.autoModels.size})",
                style = MaterialTheme.typography.titleSmall,
            )
            state.autoModels.forEach { item ->
                ModelRow(
                    item = item,
                    onToggle = { viewModel.setModelEnabled(item.model.id, it) },
                    onTest = { viewModel.testModel(item.model.id) },
                )
            }
        }

        if (state.customModels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                "手动添加 (${state.customModels.size})",
                style = MaterialTheme.typography.titleSmall,
            )
            state.customModels.forEach { item ->
                ModelRow(
                    item = item,
                    onToggle = { viewModel.setModelEnabled(item.model.id, it) },
                    onTest = { viewModel.testModel(item.model.id) },
                    onDelete = { viewModel.deleteModel(item.model.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Button(
            onClick = onAddModel,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Button,
        ) {
            Text("＋  手动添加模型")
        }
    }
}

@Composable
private fun ModelRow(
    item: ModelItem,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val alpha = if (item.model.isEnabled) 1f else 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.model.displayName,
                style = MaterialTheme.typography.bodyMedium,
            )
            val contextWindow = item.model.contextWindow
            if (contextWindow != null) {
                Text(
                    text = "${contextWindow / 1000}k",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item.testResult?.let { result ->
            Text(
                text = if (result.success) "✓ ${result.latencyMs}ms" else "✗ 失败",
                style = MaterialTheme.typography.bodySmall,
                color = if (result.success) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(AppSpacing.sm))
        }
        if (item.isTesting) {
            Text("⏳", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(AppSpacing.sm))
        } else {
            Text(
                "🔗",
                modifier = Modifier.padding(horizontal = AppSpacing.xs),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(AppSpacing.xs))
        }
        onDelete?.let {
            Text(
                "🗑️",
                modifier = Modifier.padding(horizontal = AppSpacing.xs),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = item.model.isEnabled,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun AdvancedConfigTab(viewModel: ProviderDetailViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        Text("多 API Key 轮询", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            "单个 Key 失败时自动切换",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        Text("请求超时（秒）", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        OutlinedTextField(
            value = "60",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = AppShape.InputField,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModelBottomSheet(
    state: AddModelUiState,
    onUpdate: (AddModelUiState) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AppShape.BottomSheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
        ) {
            Text("添加自定义模型", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.md))

            OutlinedTextField(
                value = state.modelId,
                onValueChange = { onUpdate(state.copy(modelId = it)) },
                label = { Text("模型 ID（必填）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            OutlinedTextField(
                value = state.displayName,
                onValueChange = { onUpdate(state.copy(displayName = it)) },
                label = { Text("显示名称（选填）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            OutlinedTextField(
                value = state.contextWindow,
                onValueChange = { onUpdate(state.copy(contextWindow = it)) },
                label = { Text("Context Window（选填）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.supportVision,
                    onCheckedChange = { onUpdate(state.copy(supportVision = it)) },
                )
                Text("支持视觉（图片输入）")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.supportFunctionCall,
                    onCheckedChange = { onUpdate(state.copy(supportFunctionCall = it)) },
                )
                Text("支持函数调用")
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.modelId.isNotBlank() && !state.isSaving,
                shape = AppShape.Button,
            ) {
                Text("添加")
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
