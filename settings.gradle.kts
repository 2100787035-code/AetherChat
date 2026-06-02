pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AetherChat"

include(":app")
include(":core:core-ui")
include(":core:core-data")
include(":core:core-network")
include(":core:core-crypto")
include(":feature:feature-chat")
include(":feature:feature-conversations")
include(":feature:feature-assistants")
include(":feature:feature-providers")
include(":feature:feature-tts")
include(":feature:feature-memory")
include(":feature:feature-tools")
include(":feature:feature-settings")
include(":domain:model")
include(":domain:usecase")
