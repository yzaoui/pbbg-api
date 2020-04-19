import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

object Versions {
    const val KOTLIN = "1.3.72"
    const val KTOR = "1.3.2"
    const val EXPOSED = "0.21.1"
    const val JUNIT_JUPITER_API = "5.6.2"
}

group = "com.bitwiserain"
version = "0.3.1"

plugins {
    application
    kotlin("jvm").version(/*Versions.KOTLIN*/"1.3.72")
    id("com.github.johnrengelman.shadow").version("5.2.0")
    id("kotlinx-serialization").version(/*Versions.KOTLIN*/"1.3.72")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = URI("https://kotlin.bintray.com/ktor") }
    maven { url = URI("https://kotlin.bintray.com/kotlinx") }
    maven { url = URI("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation(group = "com.h2database", name = "h2", version = "1.4.200")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.12")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = Versions.KOTLIN)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.20.0")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = Versions.EXPOSED)
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = Versions.EXPOSED)
    implementation(group = "at.favre.lib", name = "bcrypt", version = "0.9.0")
    implementation(group = "io.ktor", name = "ktor-server-netty", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-html-builder", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-gson", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-auth-jwt", version = Versions.KTOR)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    testImplementation(group = "io.ktor", name = "ktor-server-test-host", version = Versions.KTOR)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.JUNIT_JUPITER_API)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.JUNIT_JUPITER_API)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
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
