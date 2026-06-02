package com.aetherchat.core.data.local

import androidx.room.TypeConverter
import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.MessageStatus
import com.aetherchat.domain.model.ProviderType
import com.aetherchat.domain.model.Role
import com.aetherchat.domain.model.TestResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromRole(value: Role): String = value.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)

    @TypeConverter
    fun fromProviderType(value: ProviderType): String = value.name

    @TypeConverter
    fun toProviderType(value: String): ProviderType = ProviderType.valueOf(value)

    @TypeConverter
    fun fromTestResult(value: TestResult?): String? = value?.name

    @TypeConverter
    fun toTestResult(value: String?): TestResult? = value?.let { TestResult.valueOf(it) }

    @TypeConverter
    fun fromContentBlockList(value: List<ContentBlock>): String = json.encodeToString(value)

    @TypeConverter
    fun toContentBlockList(value: String): List<ContentBlock> = json.decodeFromString(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)
}
