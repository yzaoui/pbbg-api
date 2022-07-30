import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

object Versions {
    const val KTOR = "2.0.3"
    const val EXPOSED = "0.38.2"
    const val JUNIT_JUPITER = "5.8.1"
    const val KOTEST = "5.3.0"
    const val LOGBACK = "1.2.11"
    const val KOTLINX_SERIALIZATION = "1.3.3"
    const val POSTGRESQL = "42.4.0"
    const val H2 = "2.1.214"
    const val MOCKK = "1.12.3"
}

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    // ___KOTLIN_VERSION___
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    // ___KOTLIN_VERSION___
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.jetbrains.gradle.plugin.idea-ext")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

sourceSets {
    create("testIntegration") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val testIntegrationImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val testIntegrationRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

configurations["testIntegrationImplementation"].extendsFrom(configurations.implementation.get())
configurations["testIntegrationRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    implementation(group = "com.h2database", name = "h2", version = Versions.H2)
    implementation(group = "org.postgresql", name = "postgresql", version = Versions.POSTGRESQL)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = Versions.KOTLINX_SERIALIZATION)
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = Versions.EXPOSED)
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = Versions.EXPOSED)
    implementation(group = "at.favre.lib", name = "bcrypt", version = "0.9.0")
    implementation(group = "io.ktor", name = "ktor-server-auth-jwt-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-call-logging-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-content-negotiation-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-cors-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-netty-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-server-status-pages-jvm", version = Versions.KTOR)
    implementation(group = "io.ktor", name = "ktor-serialization-kotlinx-json-jvm", version = Versions.KTOR)
    implementation(group = "ch.qos.logback", name = "logback-classic", version = Versions.LOGBACK)

    testImplementation(kotlin("test"))
    testImplementation(group = "io.kotest", name = "kotest-assertions-core", version = Versions.KOTEST)
    testImplementation(group = "io.mockk", name = "mockk", version = Versions.MOCKK)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.JUNIT_JUPITER)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.JUNIT_JUPITER)

    testIntegrationImplementation(kotlin("test"))
    testIntegrationImplementation(group = "io.ktor", name = "ktor-server-test-host-jvm", version = Versions.KTOR)
    testIntegrationImplementation(group = "io.kotest", name = "kotest-assertions-core", version = Versions.KOTEST)
    testIntegrationImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = Versions.JUNIT_JUPITER)
    testIntegrationRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = Versions.JUNIT_JUPITER)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

val testIntegration = task<Test>("testIntegration") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["testIntegration"].output.classesDirs
    classpath = sourceSets["testIntegration"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.test {
    dependsOn(testIntegration)
}

idea.module {
    testSourceDirs.addAll(project.sourceSets["testIntegration"].allSource.srcDirs)
    settings {
        packagePrefix["src/main/kotlin"] = "com.bitwiserain.pbbg.app"
        packagePrefix["src/test/kotlin"] = "com.bitwiserain.pbbg.app.test"
        packagePrefix["src/testIntegration/kotlin"] = "com.bitwiserain.pbbg.app.testintegration"
    }
}
