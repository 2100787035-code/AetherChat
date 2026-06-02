package com.aetherchat.app

import android.app.Application
import com.aetherchat.core.crypto.coreCryptoModule
import com.aetherchat.core.data.coreDataModule
import com.aetherchat.core.network.coreNetworkModule
import com.aetherchat.domain.usecase.domainModule
import com.aetherchat.feature.chat.featureChatModule
import com.aetherchat.feature.conversations.featureConversationsModule
import com.aetherchat.feature.providers.featureProvidersModule
import com.aetherchat.feature.settings.featureSettingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AetherChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AetherChatApp)
            modules(
                coreCryptoModule,
                coreDataModule,
                coreNetworkModule,
                domainModule,
                featureProvidersModule,
                featureChatModule,
                featureConversationsModule,
                featureSettingsModule,
            )
        }
    }
}
