import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

object Versions {
    const val KOTLIN = "1.6.10"
    const val KTOR = "1.6.4"
    const val EXPOSED = "0.37.3"
    const val JUNIT_JUPITER = "5.8.1"
}

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow").version("7.1.0")
    kotlin("plugin.serialization").version(/*Versions.KOTLIN*/"1.6.10")
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    id("org.jetbrains.dokka") version /*Versions.KOTLIN*/"1.6.10"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(group = "com.h2database", name = "h2", version = "2.1.210")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.18")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.3.0")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = Versions.EXPOSED)
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = Versions.EXPOSED)
    implementation(group = "at.favre.lib", name = "bcrypt", version = "0.9.0")
    implementation(group = "io.ktor", name = "ktor-server-netty", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-gson", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-auth-jwt", version = Versions.KTOR)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    testImplementation(group = "io.ktor", name = "ktor-server-test-host", version = Versions.KTOR)
    testImplementation(group = "io.kotest", name = "kotest-assertions-core", version = "4.6.3")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.JUNIT_JUPITER)
    testImplementation(kotlin("test"))
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.JUNIT_JUPITER)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}
