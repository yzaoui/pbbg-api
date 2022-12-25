plugins {
    // ___KOTLIN_VERSION___
    kotlin("jvm") version "1.7.21" apply false
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7" apply false
    // __KTOR_VERSION
    id("io.ktor.plugin") version "2.1.3" apply false
}

group = "com.bitwiserain"
version = "0.3.1" // should mirror version in Const.kt

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

/**
 * Helper task to reload resources without restarting server
 */
tasks.register<Copy>("copyResourcesToOut") {
    from("src/main/resources") {
        include("**/*")
        destinationDir = File("out/production/resources")
    }
}
