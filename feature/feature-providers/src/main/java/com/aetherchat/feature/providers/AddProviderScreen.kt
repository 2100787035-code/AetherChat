package com.aetherchat.feature.providers

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing
import com.aetherchat.domain.model.PROVIDER_PRESETS
import com.aetherchat.domain.model.ProviderType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddProviderScreen(
    viewModel: AddProviderViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加提供商") },
                navigationIcon = {
                    Text(
                        "✕",
                        modifier = Modifier
                            .clickable(onClick = onBack)
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
                .padding(horizontal = AppSpacing.md),
        ) {
            Text("选择类型", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            val presetTypes = PROVIDER_PRESETS.filter { it.isPreset }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                presetTypes.forEach { preset ->
                    val isSelected = uiState.selectedType == preset.type
                    Box(
                        modifier = Modifier
                            .clip(AppShape.Chip)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = AppShape.Chip,
                            )
                            .clickable { viewModel.selectType(preset.type) }
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${preset.iconEmoji} ${preset.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(AppSpacing.md))

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            OutlinedTextField(
                value = uiState.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                label = { Text("Base URL（可修改）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            Button(
                onClick = viewModel::testConnection,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isTesting && uiState.selectedType != null && uiState.apiKey.isNotBlank(),
                shape = AppShape.Button,
            ) {
                if (uiState.isTesting) {
                    Text("测试中…")
                } else {
                    Text("🔗  测试连接")
                }
            }

            uiState.testResult?.let { result ->
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
                onClick = {
                    viewModel.saveProvider()
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && uiState.selectedType != null && uiState.apiKey.isNotBlank(),
                shape = AppShape.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("保存并自动检索模型")
            }
        }
    }
}
