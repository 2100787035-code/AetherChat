package com.aetherchat.core.crypto

import org.koin.dsl.module

val coreCryptoModule = module {
    single<KeystoreEncryptor> { AetherChatKeystoreEncryptor() }
}
