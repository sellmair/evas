plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    `maven-publish`
    com.vanniktech.maven.publish
}

description = "Compose (Multiplatform) extensions for evas"

kotlin {
    jvm()

    macosArm64()
    macosX64()

    iosSimulatorArm64()
    iosArm64()
    iosX64()

    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        compileOnly(compose.foundation)
    }
}

