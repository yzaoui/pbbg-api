rootProject.name = "pbbg"

pluginManagement {
    plugins {
        val KOTLIN_VERSION = "1.9.22"
        kotlin("jvm") version KOTLIN_VERSION
        kotlin("plugin.serialization") version KOTLIN_VERSION
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
        // __KTOR_VERSION
        id("io.ktor.plugin") version "2.3.0"
        id("org.jetbrains.kotlinx.kover") version "0.6.1"
        id("org.jetbrains.dokka") version "1.7.20"
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = java.net.URI("https://kotlin.bintray.com/ktor") }
        maven { url = java.net.URI("https://kotlin.bintray.com/kotlinx") }
        maven { url = java.net.URI("https://dl.bintray.com/kotlin/exposed") }
    }
}

include("app")
