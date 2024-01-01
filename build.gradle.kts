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
