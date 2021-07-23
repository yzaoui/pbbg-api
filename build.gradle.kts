import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI

object Versions {
    const val KOTLIN = "1.5.21"
    const val KTOR = "1.6.1"
    const val EXPOSED = "0.32.1"
    const val JUNIT_JUPITER = "5.7.2"
}

group = "com.bitwiserain"
version = "0.3.1" // should mirror version in Const.kt

plugins {
    application
    kotlin("jvm").version(/*Versions.KOTLIN*/"1.5.21")
    id("com.github.johnrengelman.shadow").version("7.0.0")
    kotlin("plugin.serialization").version(/*Versions.KOTLIN*/"1.5.21")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = URI("https://kotlin.bintray.com/ktor") }
    maven { url = URI("https://kotlin.bintray.com/kotlinx") }
    maven { url = URI("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation(group = "com.h2database", name = "h2", version = "1.4.200")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.18")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.0.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = Versions.EXPOSED)
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = Versions.EXPOSED)
    implementation(group = "at.favre.lib", name = "bcrypt", version = "0.9.0")
    implementation(group = "io.ktor", name = "ktor-server-netty", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-gson", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-auth-jwt", version = Versions.KTOR)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    testImplementation(group = "io.ktor", name = "ktor-server-test-host", version = Versions.KTOR)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.JUNIT_JUPITER)
    testImplementation(kotlin("test"))
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.JUNIT_JUPITER)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
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
