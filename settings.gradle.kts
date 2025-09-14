// In settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // RepositoriesMode.FAIL_ON_PROJECT_REPOS é a configuração moderna e recomendada.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        // Esta é a linha crucial. Ela diz ao Gradle para procurar bibliotecas no JitPack.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "TCC"
include(":app")