package com.aetherchat.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        when {
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage!!)
                viewModel.clearError()
            }
            uiState.successMessage != null -> {
                snackbarHostState.showSnackbar(uiState.successMessage!!)
                viewModel.clearSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.isLoading || uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AiChatSection(viewModel, uiState)
            WebSearchSection(viewModel, uiState)
            VoiceSection(viewModel, uiState)
            DataSyncSection(viewModel, uiState)
            AppearanceSection(viewModel, uiState)
            AboutSection(uiState)

            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded) {
            content()
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.md))
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SettingsStringDropdown(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedValue }?.second ?: selectedValue
    Box(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(selectedLabel) },
            modifier = Modifier.clickable { expanded = true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsIntDropdown(
    title: String,
    options: List<Pair<Int, String>>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedValue }?.second ?: selectedValue.toString()
    Box(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(selectedLabel) },
            modifier = Modifier.clickable { expanded = true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onValueSelected(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsTextFieldItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                }
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = 4.dp),
        singleLine = true,
    )
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: (Float) -> String = { it.toString() },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                valueLabel(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SubSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}

@Composable
private fun AiChatSection(viewModel: SettingsViewModel, uiState: SettingsUiState) {
    ExpandableSection(title = "AI 对话设置") {
        SettingsStringDropdown(
            title = "默认提供商",
            options = uiState.availableProviders.map { it.id to it.name },
            selectedValue = uiState.defaultProviderId,
            onValueSelected = viewModel::updateDefaultProvider,
        )
        SettingsStringDropdown(
            title = "默认模型",
            options = uiState.availableModels.map { it.id to it.displayName },
            selectedValue = uiState.defaultModelId,
            onValueSelected = viewModel::updateDefaultModel,
        )
        SettingsSwitchItem(
            title = "流式响应",
            checked = uiState.streamResponse,
            onCheckedChange = viewModel::updateStreamResponse,
        )
        SettingsSwitchItem(
            title = "Enter 发送",
            checked = uiState.sendOnEnter,
            onCheckedChange = viewModel::updateSendOnEnter,
        )
        SettingsSwitchItem(
            title = "显示 Token 计数",
            checked = uiState.showTokenCount,
            onCheckedChange = viewModel::updateShowTokenCount,
        )
    }
}

@Composable
private fun WebSearchSection(viewModel: SettingsViewModel, uiState: SettingsUiState) {
    ExpandableSection(title = "网络搜索设置") {
        SettingsSwitchItem(
            title = "启用网络搜索",
            checked = uiState.webSearchEnabled,
            onCheckedChange = viewModel::updateWebSearchEnabled,
        )
        SettingsStringDropdown(
            title = "搜索引擎",
            options = listOf("searxng" to "SearXNG"),
            selectedValue = uiState.webSearchEngine,
            onValueSelected = viewModel::updateWebSearchEngine,
        )
        SettingsTextFieldItem(
            label = "SearXNG 服务地址",
            value = uiState.searxngUrl,
            onValueChange = viewModel::updateSearxngUrl,
        )
    }
}

@Composable
private fun VoiceSection(viewModel: SettingsViewModel, uiState: SettingsUiState) {
    ExpandableSection(title = "语音设置") {
        SubSectionHeader(title = "STT 语音识别")
        SettingsSwitchItem(
            title = "启用 STT",
            checked = uiState.sttEnabled,
            onCheckedChange = viewModel::updateSttEnabled,
        )
        SettingsStringDropdown(
            title = "STT 提供商",
            options = listOf("openai_whisper" to "OpenAI Whisper"),
            selectedValue = uiState.sttProvider,
            onValueSelected = viewModel::updateSttProvider,
        )
        SettingsTextFieldItem(
            label = "API 密钥",
            value = uiState.sttApiKey,
            onValueChange = viewModel::updateSttApiKey,
            isPassword = true,
        )
        SettingsTextFieldItem(
            label = "服务地址",
            value = uiState.sttBaseUrl,
            onValueChange = viewModel::updateSttBaseUrl,
        )
        SettingsTextFieldItem(
            label = "模型",
            value = uiState.sttModel,
            onValueChange = viewModel::updateSttModel,
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs))

        SubSectionHeader(title = "TTS 语音合成")
        SettingsSwitchItem(
            title = "启用 TTS",
            checked = uiState.ttsEnabled,
            onCheckedChange = viewModel::updateTtsEnabled,
        )
        SettingsStringDropdown(
            title = "TTS 提供商",
            options = listOf("openai_tts" to "OpenAI TTS"),
            selectedValue = uiState.ttsProvider,
            onValueSelected = viewModel::updateTtsProvider,
        )
        SettingsTextFieldItem(
            label = "API 密钥",
            value = uiState.ttsApiKey,
            onValueChange = viewModel::updateTtsApiKey,
            isPassword = true,
        )
        SettingsTextFieldItem(
            label = "服务地址",
            value = uiState.ttsBaseUrl,
            onValueChange = viewModel::updateTtsBaseUrl,
        )
        SettingsStringDropdown(
            title = "语音",
            options = listOf(
                "alloy" to "Alloy",
                "echo" to "Echo",
                "fable" to "Fable",
                "onyx" to "Onyx",
                "nova" to "Nova",
                "shimmer" to "Shimmer",
            ),
            selectedValue = uiState.ttsVoice,
            onValueSelected = viewModel::updateTtsVoice,
        )
        SettingsSliderItem(
            title = "语速",
            value = uiState.ttsSpeed,
            onValueChange = viewModel::updateTtsSpeed,
            valueRange = 0.5f..2.0f,
            steps = 14,
            valueLabel = { String.format("%.1fx", it) },
        )
    }
}

@Composable
private fun DataSyncSection(viewModel: SettingsViewModel, uiState: SettingsUiState) {
    ExpandableSection(title = "数据同步") {
        SubSectionHeader(title = "WebDAV 同步")
        SettingsSwitchItem(
            title = "启用同步",
            checked = uiState.webdavEnabled,
            onCheckedChange = viewModel::updateWebdavEnabled,
        )
        SettingsTextFieldItem(
            label = "服务器地址",
            value = uiState.webdavServerUrl,
            onValueChange = viewModel::updateWebdavServerUrl,
        )
        SettingsTextFieldItem(
            label = "用户名",
            value = uiState.webdavUsername,
            onValueChange = viewModel::updateWebdavUsername,
        )
        SettingsTextFieldItem(
            label = "密码",
            value = uiState.webdavPassword,
            onValueChange = viewModel::updateWebdavPassword,
            isPassword = true,
        )
        SettingsTextFieldItem(
            label = "远程路径",
            value = uiState.webdavRemotePath,
            onValueChange = viewModel::updateWebdavRemotePath,
        )
        SettingsSwitchItem(
            title = "自动同步",
            checked = uiState.webdavAutoSync,
            onCheckedChange = viewModel::updateWebdavAutoSync,
        )
        SettingsIntDropdown(
            title = "同步间隔",
            options = listOf(
                15 to "15 分钟",
                30 to "30 分钟",
                60 to "1 小时",
                360 to "6 小时",
                720 to "12 小时",
                1440 to "24 小时",
            ),
            selectedValue = uiState.webdavSyncIntervalMinutes,
            onValueSelected = viewModel::updateWebdavSyncInterval,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            FilledTonalButton(
                onClick = viewModel::syncNow,
                enabled = uiState.webdavEnabled && !uiState.isSyncing,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (uiState.isSyncing) "同步中..." else "立即同步")
            }
            OutlinedButton(
                onClick = viewModel::testWebdavConnection,
                enabled = uiState.webdavEnabled && !uiState.isLoading,
                modifier = Modifier.weight(1f),
            ) {
                Text("测试连接")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs))

        ListItem(
            headlineContent = { Text("数据导出") },
            modifier = Modifier.clickable { viewModel.exportData() },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun AppearanceSection(viewModel: SettingsViewModel, uiState: SettingsUiState) {
    ExpandableSection(title = "外观设置") {
        SettingsStringDropdown(
            title = "主题模式",
            options = listOf(
                "system" to "跟随系统",
                "light" to "浅色",
                "dark" to "深色",
            ),
            selectedValue = uiState.themeMode,
            onValueSelected = viewModel::updateThemeMode,
        )
        SettingsSwitchItem(
            title = "动态颜色",
            checked = uiState.dynamicColorEnabled,
            onCheckedChange = viewModel::updateDynamicColor,
        )
        SettingsSliderItem(
            title = "字体大小",
            value = uiState.fontSize.toFloat(),
            onValueChange = { viewModel.updateFontSize(it.toInt()) },
            valueRange = 12f..24f,
            steps = 11,
            valueLabel = { "${it.toInt()}sp" },
        )
    }
}

@Composable
private fun AboutSection(uiState: SettingsUiState) {
    val context = LocalContext.current

    ExpandableSection(title = "关于", initiallyExpanded = false) {
        ListItem(
            headlineContent = { Text("应用版本") },
            supportingContent = { Text(uiState.appVersion) },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            headlineContent = { Text("开源许可") },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.clickable { },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        ListItem(
            headlineContent = { Text("项目主页") },
            leadingContent = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aetherchat/aetherchat"))
                context.startActivity(intent)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
