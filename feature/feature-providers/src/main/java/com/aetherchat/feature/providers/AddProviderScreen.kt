package com.aetherchat.feature.providers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing
import com.aetherchat.domain.model.PROVIDER_PRESETS
import com.aetherchat.domain.model.ProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProviderScreen(
    viewModel: AddProviderViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedProviderId) {
        if (uiState.savedProviderId != null) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加提供商") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
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
            Text("选择提供商类型", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            ProviderTypeGrid(
                selectedType = uiState.selectedType,
                onSelect = viewModel::selectType,
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            if (uiState.selectedType != null) {
                OutlinedTextField(
                    value = uiState.baseUrl,
                    onValueChange = viewModel::updateBaseUrl,
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))

                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = AppShape.InputField,
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))

                Button(
                    onClick = viewModel::testConnection,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isTesting && uiState.apiKey.isNotBlank(),
                    shape = AppShape.Button,
                ) {
                    Text(if (uiState.isTesting) "测试中…" else "🔗  测试连接")
                }

                uiState.testResult?.let { result ->
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = if (result.success)
                            "✅  连接成功  延迟 ${result.latencyMs}ms  可用模型 ${result.modelCount}"
                        else
                            "❌  连接失败: ${result.errorMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (result.success) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                Button(
                    onClick = viewModel::saveProvider,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && uiState.apiKey.isNotBlank(),
                    shape = AppShape.Button,
                ) {
                    Text(if (uiState.isSaving) "保存中…" else "💾  保存提供商")
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
    }
}

@Composable
private fun ProviderTypeGrid(
    selectedType: ProviderType?,
    onSelect: (ProviderType) -> Unit,
) {
    Column {
        PROVIDER_PRESETS.chunked(3).forEach { row ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { preset ->
                    androidx.compose.material3.FilterChip(
                        selected = selectedType == preset.type,
                        onClick = { onSelect(preset.type) },
                        label = { Text("${preset.iconEmoji} ${preset.displayName}") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(AppSpacing.xs),
                        shape = AppShape.Button,
                    )
                }
                val remaining = 3 - row.size
                repeat(remaining) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
