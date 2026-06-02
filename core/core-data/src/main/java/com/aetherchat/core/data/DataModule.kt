package com.aetherchat.core.data

import androidx.room.Room
import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.repository.ProviderRepositoryImpl
import com.aetherchat.domain.model.ProviderRepository
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule = module {
    single<AetherChatDatabase> {
        val passphrase = "aetherchat_db_key_2026".toByteArray()
        val factory = SupportFactory(passphrase)
        Room.databaseBuilder(
            androidContext(),
            AetherChatDatabase::class.java,
            "aetherchat.db"
        )
            .openHelperFactory(factory)
            .build()
    }

    single<ProviderRepository> {
        ProviderRepositoryImpl(get(), get())
    }
}
