package com.aetherchat.app

import android.app.Application
import com.aetherchat.core.crypto.coreCryptoModule
import com.aetherchat.core.data.coreDataModule
import com.aetherchat.core.network.coreNetworkModule
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
            )
        }
    }
}
