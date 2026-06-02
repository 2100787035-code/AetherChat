plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.aetherchat.domain.usecase"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-network"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
}
