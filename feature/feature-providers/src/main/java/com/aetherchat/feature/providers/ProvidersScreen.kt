package com.aetherchat.feature.providers

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    viewModel: ProvidersViewModel,
    onAddProvider: () -> Unit,
    onProviderClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提供商管理") },
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
                .padding(padding),
        ) {
            Button(
                onClick = onAddProvider,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                shape = AppShape.Button,
            ) {
                Text("＋  添加提供商")
            }

            if (uiState.isLoading) {
                Text(
                    "加载中…",
                    modifier = Modifier.padding(AppSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (uiState.providers.isEmpty()) {
                Text(
                    "暂无提供商，点击上方按钮添加",
                    modifier = Modifier.padding(AppSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn {
                    items(uiState.providers, key = { it.provider.id }) { item ->
                        ProviderRow(
                            item = item,
                            onToggle = { viewModel.setProviderEnabled(item.provider.id, it) },
                            onClick = { onProviderClick(item.provider.id) },
                            onDelete = { viewModel.deleteProvider(item.provider.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(AppSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    item: ProviderItem,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val alpha = if (item.provider.isEnabled) 1f else 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.iconEmoji,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.provider.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = item.provider.type.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "🗑️",
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(horizontal = AppSpacing.xs),
        )
        Switch(
            checked = item.provider.isEnabled,
            onCheckedChange = onToggle,
        )
    }
}
