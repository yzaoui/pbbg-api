import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

object Versions {
    const val KOTLIN = "1.3.41"
    const val KTOR = "1.2.3"
}

group = "com.bitwiserain"
version = "0.3.0"

plugins {
    application
    kotlin("jvm").version(/*Versions.KOTLIN*/"1.3.41")
    id("com.github.johnrengelman.shadow").version("5.0.0")
    id("kotlinx-serialization").version(/*Versions.KOTLIN*/"1.3.41")
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
    implementation(group = "com.h2database", name = "h2", version = "1.4.197")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.1")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = Versions.KOTLIN)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.11.0")
    implementation(group = "org.jetbrains.exposed", name = "exposed", version = "0.17.2")
    implementation(group = "at.favre.lib", name = "bcrypt", version = "0.7.0")
    implementation(group = "io.ktor", name = "ktor-locations", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-netty", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-html-builder", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-gson", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-auth-jwt", version = Versions.KTOR)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = "0.6.10")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    testImplementation(group = "io.ktor", name = "ktor-server-test-host", version = Versions.KTOR)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.4.2")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.4.2")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    baseName = "pbbg"
    classifier = ""
    version = ""
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
