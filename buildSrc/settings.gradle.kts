pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}