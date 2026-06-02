# AetherChat - Project Rules

## Architecture
- MVVM + ViewModel + StateFlow + UiState
- Navigation Compose Type-safe Routes
- Koin DI
- Multi-module: app / core:* / feature:* / domain:*

## Tech Stack (mandatory)
- Kotlin 100%, no Java
- Jetpack Compose + Material Design 3
- OkHttp + Retrofit for SSE streaming
- Room + SQLCipher (AES-256)
- DataStore Proto
- Coil image loading
- Android Keystore + Tink encryption
- Gradle Kotlin DSL

## Coding Conventions
- Conventional Commits: feat: / fix: / chore: / refactor:
- File header: // feature-chat · ChatScreen.kt / 对话页面主入口
- Use collectAsStateWithLifecycle()
- No Composable doing DB/network ops
- No mutableStateOf for business data
- No hardcoded colors or spacing (use AppColors, AppShape, AppSpacing)
- Repository returns Result<T>, ViewModel converts to errorMessage: String?
- UI uses Snackbar for errors, never AlertDialog for network errors
- API Key via core-crypto KeystoreEncryptor, never plaintext in logs

## Branch Strategy
- main: only accepts PRs from develop
- feature/xxx: branches from develop, merges back to develop
- develop: integration branch

## Prohibitions
- RxJava, XML layouts, hardcoded strings
- Main thread runBlocking
- Plaintext API Key storage
- Ollama or any local model
- Repository layer depending on Android Context
- Cross-feature internal class references
