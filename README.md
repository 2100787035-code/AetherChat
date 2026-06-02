# AetherChat

Privacy-first AI Chat Client for Android — one interface, all AI providers.

## Features
- Multi-provider support: MiMo, DeepSeek, Mistral, Groq, Aliyun, Moonshot, SiliconFlow, and custom OpenAI-compatible endpoints
- SSE streaming with real-time token rendering
- Local encrypted storage (Room + SQLCipher AES-256)
- API keys encrypted with Android Keystore
- Material Design 3 with dynamic color
- Onboarding flow for first-time setup

## Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + ViewModel + StateFlow |
| DI | Koin |
| Network | OkHttp + Retrofit + SSE |
| Database | Room + SQLCipher |
| Storage | DataStore Proto |
| Crypto | Android Keystore + Tink |
| Image | Coil |
| Navigation | Navigation Compose (type-safe routes) |
| Build | Gradle Kotlin DSL, multi-module |

## Module Structure
```
AetherChat/
├── app/                    # Application entry point
├── core/
│   ├── core-ui/            # Theme, colors, shapes, spacing
│   ├── core-data/          # Room DB, repositories
│   ├── core-network/       # OkHttp, Retrofit, SSE
│   └── core-crypto/        # Keystore encryption
├── feature/
│   ├── feature-chat/       # Chat screen
│   ├── feature-conversations/ # Conversation list
│   ├── feature-assistants/ # Assistant management
│   ├── feature-providers/  # Provider management
│   ├── feature-tts/        # Text-to-speech
│   ├── feature-memory/     # Cross-session memory
│   ├── feature-tools/      # Tool/function calling
│   └── feature-settings/   # App settings
└── domain/
    ├── model/              # Core data models
    └── usecase/            # Business logic
```

## Build
```bash
./gradlew assembleDebug
```

## Test
```bash
./gradlew testDebugUnitTest
```

## CI
GitHub Actions runs on push to `develop`/`main` and PRs to `main`:
- Build Debug APK
- Run Unit Tests

## License
Private repository. All rights reserved.
