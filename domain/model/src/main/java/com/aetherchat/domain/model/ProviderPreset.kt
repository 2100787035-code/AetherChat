package com.aetherchat.domain.model

data class ProviderPreset(
    val type: ProviderType,
    val displayName: String,
    val defaultBaseUrl: String,
    val isPreset: Boolean,
    val iconEmoji: String,
)

val PROVIDER_PRESETS = listOf(
    ProviderPreset(ProviderType.XIAOMI_MIMO, "小米 MiMo", "https://api.xiaomimimo.com/v1", true, "🟠"),
    ProviderPreset(ProviderType.DEEPSEEK, "DeepSeek", "https://api.deepseek.com/v1", true, "🔷"),
    ProviderPreset(ProviderType.MISTRAL, "Mistral", "https://api.mistral.ai/v1", true, "🔵"),
    ProviderPreset(ProviderType.GROQ, "Groq", "https://api.groq.com/openai/v1", true, "⚡"),
    ProviderPreset(ProviderType.ALIYUN_BAILIAN, "阿里云百炼", "https://dashscope.aliyuncs.com/compatible-mode/v1", true, "🟡"),
    ProviderPreset(ProviderType.MOONSHOT, "月之暗面", "https://api.moonshot.cn/v1", true, "🌙"),
    ProviderPreset(ProviderType.SILICONFLOW, "硅基流动", "https://api.siliconflow.cn/v1", true, "🔮"),
    ProviderPreset(ProviderType.CUSTOM_OPENAI_COMPAT, "自定义", "", true, "⚙️"),
    ProviderPreset(ProviderType.OPENAI, "OpenAI", "https://api.openai.com/v1", false, "🟢"),
    ProviderPreset(ProviderType.ANTHROPIC, "Anthropic", "https://api.anthropic.com", false, "🟣"),
    ProviderPreset(ProviderType.GOOGLE, "Google", "https://generativelanguage.googleapis.com", false, "🔴"),
)
