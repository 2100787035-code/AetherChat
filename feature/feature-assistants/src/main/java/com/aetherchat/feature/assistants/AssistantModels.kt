package com.aetherchat.feature.assistants

import kotlinx.serialization.Serializable

@Serializable
data class Assistant(
    val id: String,
    val name: String,
    val iconEmoji: String = "🤖",
    val systemPrompt: String = "",
    val providerId: String? = null,
    val modelId: String? = null,
    val temperature: Float = 0.7f,
    val createdAt: Long,
    val updatedAt: Long,
)

data class AssistantsUiState(
    val assistants: List<Assistant> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class CreateAssistantUiState(
    val name: String = "",
    val iconEmoji: String = "🤖",
    val systemPrompt: String = "",
    val providerId: String? = null,
    val modelId: String? = null,
    val temperature: String = "0.7",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

val PRESET_ASSISTANTS = listOf(
    PresetAssistant("写作助手", "✍️", "你是一位专业的写作助手，擅长帮助用户撰写、润色和优化各类文本。请根据用户的需求提供高质量的写作建议和内容。"),
    PresetAssistant("编程助手", "💻", "你是一位经验丰富的编程助手，精通多种编程语言和框架。请帮助用户解决编程问题、优化代码、解释技术概念。"),
    PresetAssistant("翻译助手", "🌐", "你是一位专业的翻译助手，支持多种语言之间的互译。请提供准确、自然的翻译结果，并保留原文的语气和风格。"),
    PresetAssistant("学习助手", "📚", "你是一位耐心的学习助手，擅长用简单易懂的方式解释复杂概念。请帮助用户理解知识点、解答疑问、提供学习建议。"),
    PresetAssistant("创意助手", "🎨", "你是一位富有创意的助手，擅长头脑风暴、创意构思和灵感激发。请帮助用户产生新想法、拓展思路、提供创新方案。"),
)

data class PresetAssistant(
    val name: String,
    val iconEmoji: String,
    val systemPrompt: String,
)
