package com.aetherchat.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object AppShape {
    val UserBubble = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    val AssistantBubble = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    val InputField = RoundedCornerShape(24.dp)
    val Card = RoundedCornerShape(16.dp)
    val BottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val Dialog = RoundedCornerShape(20.dp)
    val Chip = RoundedCornerShape(12.dp)
    val Button = RoundedCornerShape(12.dp)
}
