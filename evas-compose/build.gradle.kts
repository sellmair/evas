import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    `maven-publish`
}

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
