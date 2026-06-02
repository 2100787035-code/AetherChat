package com.aetherchat.app.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppSpacing

data class MessageBranch(
    val id: String,
    val parentId: String?,
    val label: String,
    val isActive: Boolean,
)

@Composable
fun MessageBranchSelector(
    branches: List<MessageBranch>,
    onBranchSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (branches.size <= 1) return

    Row(
        modifier = modifier.padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        branches.forEachIndexed { index, branch ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }

            val isActive = branch.isActive
            Surface(
                modifier = Modifier.clickable { onBranchSelected(branch.id) },
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
