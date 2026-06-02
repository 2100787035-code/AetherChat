package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val type: ProviderType,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)

@Serializable
enum class ProviderType {
    XIAOMI_MIMO,
    DEEPSEEK,
    MISTRAL,
    GROQ,
    ALIYUN_BAILIAN,
    MOONSHOT,
    SILICONFLOW,
    CUSTOM_OPENAI_COMPAT,
    OPENAI,
    ANTHROPIC,
    GOOGLE,
}
