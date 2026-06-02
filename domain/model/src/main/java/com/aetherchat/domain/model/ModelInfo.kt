package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelInfo(
    val id: String,
    val providerId: String,
    val displayName: String,
    val contextWindow: Int? = null,
    val supportVision: Boolean = false,
    val supportFunctionCall: Boolean = false,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val lastTestedAt: Long? = null,
    val lastTestResult: TestResult? = null,
)

@Serializable
enum class TestResult {
    SUCCESS, FAILED, UNTESTED
}
