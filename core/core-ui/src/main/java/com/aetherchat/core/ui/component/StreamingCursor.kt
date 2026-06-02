package com.aetherchat.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun StreamingCursor(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )

    androidx.compose.material3.Text(
        text = "▌",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
        modifier = modifier.graphicsLayer { this.alpha = alpha },
    )
}
