package com.aetherchat.feature.assistants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAssistantScreen(
    viewModel: CreateAssistantViewModel,
    onCreated: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建助手") },
                navigationIcon = {
                    Text(
                        "✕",
                        modifier = Modifier.clickable(onClick = onBack).padding(AppSpacing.md),
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
                .padding(horizontal = AppSpacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("选择预设", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PRESET_ASSISTANTS.forEach { preset ->
                    Surface(
                        modifier = Modifier.clickable { viewModel.loadPreset(preset) },
                        shape = AppShape.Chip,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        ) {
                            Text(preset.iconEmoji, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                preset.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

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

            OutlinedTextField(
                value = uiState.temperature,
                onValueChange = viewModel::updateTemperature,
                label = { Text("Temperature") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Button(
                onClick = {
                    viewModel.createAssistant()
                    onCreated()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = AppShape.Button,
            ) {
                Text("创建助手")
            }

            Spacer(modifier = Modifier.height(AppSpacing.xl))
        }
    }
}
