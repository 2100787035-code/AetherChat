package com.aetherchat.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSection(title = "外观") {
                SettingsRow(
                    title = "主题",
                    value = uiState.themeMode.label,
                    onClick = { },
                )
                SettingsRow(
                    title = "主题颜色",
                    value = if (uiState.dynamicColorEnabled) "动态取色" else "默认",
                    onClick = { },
                )
                SettingsRow(
                    title = "字体大小",
                    value = uiState.fontSizeLevel.label,
                    onClick = { },
                )
            }

            SettingsSection(title = "语音") {
                SettingsRow(
                    title = "TTS 服务",
                    value = uiState.ttsService,
                    onClick = { },
                )
                SettingsRow(
                    title = "音色",
                    value = "Alloy",
                    onClick = { },
                )
                SettingsRow(
                    title = "语速",
                    value = "1.0x",
                    onClick = { },
                )
                SettingsToggleRow(
                    title = "自动朗读",
                    checked = uiState.autoReadEnabled,
                    onCheckedChange = { },
                )
            }

            SettingsSection(title = "对话") {
                SettingsRow(
                    title = "默认模型",
                    value = uiState.defaultModel,
                    onClick = { },
                )
                SettingsToggleRow(
                    title = "流式响应",
                    checked = uiState.streamEnabled,
                    onCheckedChange = viewModel::updateStreamEnabled,
                )
                SettingsRow(
                    title = "发送方式",
                    value = uiState.sendMethod.label,
                    onClick = { },
                )
            }

            SettingsSection(title = "数据") {
                SettingsRow(title = "导出会话", value = "", onClick = { })
                SettingsRow(title = "导入会话", value = "", onClick = { })
                SettingsRow(title = "清空所有数据", value = "", onClick = { })
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
    content()
    HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.md))
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotBlank()) {
            Text(
                text = "$value ›",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
