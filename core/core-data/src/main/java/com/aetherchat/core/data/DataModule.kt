package com.aetherchat.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.repository.ProviderRepositoryImpl
import com.aetherchat.domain.model.ProviderRepository
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule = module {
    single<SharedPreferences> {
        androidContext().getSharedPreferences("aetherchat_settings", Context.MODE_PRIVATE)
    }

    single<AetherChatDatabase> {
        System.loadLibrary("sqlcipher")
        val passphrase = "aetherchat_db_key_2026".toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)
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
