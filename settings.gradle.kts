rootProject.name = "pbbg"

// This block is only here while kotlinx.serialization remains unpublished
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
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
