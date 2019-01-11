import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

object Versions {
    const val KOTLIN = "1.3.11"
    const val KTOR = "1.1.1"
}

group = "com.bitwiserain"
version = "0.0.1"

plugins {
    application
    kotlin("jvm").version(/*Versions.KOTLIN*/"1.3.11")
    id("com.github.johnrengelman.shadow").version("4.0.3")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = URI("https://kotlin.bintray.com/ktor") }
    maven { url = URI("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    compile("com.h2database:h2:1.4.197")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.KOTLIN}")
    compile("org.jetbrains.exposed:exposed:0.11.2")
    compile("at.favre.lib:bcrypt:0.6.0")
    compile("io.ktor:ktor-locations:${Versions.KTOR}")
    compile("io.ktor:ktor-server-netty:${Versions.KTOR}")
    compile("io.ktor:ktor-html-builder:${Versions.KTOR}")
    compile("io.ktor:ktor-gson:${Versions.KTOR}")
    compile("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")
    compile("ch.qos.logback:logback-classic:1.2.3")

    testCompile("io.ktor:ktor-server-test-host:${Versions.KTOR}")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
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
