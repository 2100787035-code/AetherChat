package com.aetherchat.core.network

import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.network.provider.OpenAICompatProvider
import com.aetherchat.domain.model.LLMProvider
import com.aetherchat.domain.model.PROVIDER_PRESETS
import com.aetherchat.domain.model.ProviderType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val coreNetworkModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }
}
