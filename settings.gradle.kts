@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "junk-code"
