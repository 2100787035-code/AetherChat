package com.aetherchat.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `assistants` (" +
                        "`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                        "`iconEmoji` TEXT NOT NULL DEFAULT '🤖', `systemPrompt` TEXT NOT NULL DEFAULT '', " +
                        "`providerId` TEXT, `modelId` TEXT, " +
                        "`temperature` REAL NOT NULL DEFAULT 0.7, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }
        Room.databaseBuilder(
            androidContext(),
            AetherChatDatabase::class.java,
            "aetherchat.db"
        )
            .openHelperFactory(factory)
            .addMigrations(migration1To2)
            .build()
    }

    single<ProviderRepository> {
        ProviderRepositoryImpl(get(), get())
    }
}
