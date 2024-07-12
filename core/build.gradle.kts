import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.atomicfu")
    `maven-publish`
}

kotlin {
    jvm()
    linuxX64()
    linuxX64()

    macosArm64()
    macosX64()

    iosSimulatorArm64()
    iosArm64()
    iosX64()

    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    /*
    Dependencies
     */

    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    }

    /*
    Compiler Options
     */

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        explicitApi()
    }

    /*
    Jvm Options
     */

    jvmToolchain(17)
}

