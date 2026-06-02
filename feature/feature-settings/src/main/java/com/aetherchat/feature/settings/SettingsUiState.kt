package com.aetherchat.feature.settings

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val fontSizeLevel: FontSizeLevel = FontSizeLevel.STANDARD,
    val ttsService: String = "OpenAI",
    val ttsVoice: String = "Alloy",
    val ttsSpeed: String = "1.0x",
    val autoReadEnabled: Boolean = false,
    val defaultModel: String = "MiMo-7B",
    val streamEnabled: Boolean = true,
    val sendMethod: SendMethod = SendMethod.ENTER,
)

enum class ThemeMode(val label: String) { SYSTEM("跟随系统"), LIGHT("浅色"), DARK("深色") }
enum class FontSizeLevel(val label: String) { SMALL("小"), STANDARD("标准"), LARGE("大"), EXTRA_LARGE("特大") }
enum class SendMethod(val label: String) { ENTER("按下发送"), CTRL_ENTER("Ctrl+Enter 发送") }
