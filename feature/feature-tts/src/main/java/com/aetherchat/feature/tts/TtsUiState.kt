package com.aetherchat.feature.tts

import com.aetherchat.domain.model.VoiceInfo

data class TtsUiState(
    val selectedService: TtsServiceType = TtsServiceType.OPENAI,
    val voices: List<VoiceInfo> = emptyList(),
    val selectedVoice: String = "alloy",
    val speed: Float = 1.0f,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

enum class TtsServiceType(val displayName: String) {
    OPENAI("OpenAI"),
    MINIMAX("MiniMax"),
    AZURE("Azure"),
    VOLCANO("火山引擎"),
    ALIYUN("阿里云"),
    ELEVENLABS("ElevenLabs"),
    FISH_AUDIO("Fish Audio"),
    OPENAI_COMPAT("OpenAI 兼容"),
}
