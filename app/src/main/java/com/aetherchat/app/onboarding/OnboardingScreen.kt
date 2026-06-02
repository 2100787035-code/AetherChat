package com.aetherchat.app.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing
import com.aetherchat.domain.model.PROVIDER_PRESETS
import com.aetherchat.domain.model.ProviderType
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToConversations: () -> Unit,
    onNavigateToAddProvider: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> AddProviderPage(onNavigateToAddProvider)
                    2 -> ChooseAssistantPage()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.lg),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onNavigateToConversations,
                ) {
                    Text("跳过")
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateToConversations()
                        }
                    },
                    shape = AppShape.Button,
                ) {
                    Text(if (pagerState.currentPage < 2) "→ 下一步" else "完成 →")
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🚀", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            "欢迎使用 AetherChat",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            "一个入口，所有 AI",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddProviderPage(onNavigateToAddProvider: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("添加你的第一个 AI Provider", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(AppSpacing.lg))

        val presetProviders = PROVIDER_PRESETS.filter { it.isPreset }
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            presetProviders.take(4).forEach { preset ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .clickable { onNavigateToAddProvider() },
                    shape = AppShape.Card,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "${preset.iconEmoji}  ${preset.displayName}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(AppSpacing.md),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseAssistantPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("选择默认助手", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(AppSpacing.lg))

        val assistants = listOf(
            "✍️" to "写作助手",
            "💻" to "编程助手",
            "🌐" to "翻译助手",
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            assistants.forEach { (emoji, name) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    shape = AppShape.Card,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "$emoji  $name",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(AppSpacing.md),
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Surface(
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = AppShape.Card,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    "空白开始",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(AppSpacing.md),
                )
            }
        }
    }
}
