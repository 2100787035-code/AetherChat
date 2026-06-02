package com.aetherchat.feature.tts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing
import com.aetherchat.domain.model.VoiceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsScreen(
    viewModel: TtsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语音设置") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.md),
        ) {
            Text("TTS 服务", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                items(TtsServiceType.entries) { service ->
                    val isSelected = uiState.selectedService == service
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectService(service) },
                        shape = AppShape.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Text(
                            text = service.displayName,
                            modifier = Modifier.padding(AppSpacing.md),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text("音色", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppSpacing.sm))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                items(uiState.voices) { voice ->
                    val isSelected = uiState.selectedVoice == voice.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectVoice(voice.id) },
                        shape = AppShape.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Text(
                            text = voice.name,
                            modifier = Modifier.padding(AppSpacing.md),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text("语速: ${String.format("%.1f", uiState.speed)}x", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = uiState.speed,
                onValueChange = viewModel::updateSpeed,
                valueRange = 0.5f..2.0f,
                steps = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            OutlinedTextField(
                value = uiState.currentText,
                onValueChange = {},
                label = { Text("试听文本") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = AppShape.InputField,
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Button(
                    onClick = { viewModel.playText("你好，我是 AetherChat 的语音助手。") },
                    modifier = Modifier.weight(1f),
                    shape = AppShape.Button,
                    enabled = !uiState.isPlaying,
                ) {
                    Text("▶ 试听")
                }
                Button(
                    onClick = viewModel::stopPlayback,
                    modifier = Modifier.weight(1f),
                    shape = AppShape.Button,
                    enabled = uiState.isPlaying,
                ) {
                    Text("■ 停止")
                }
            }
        }
    }
}
