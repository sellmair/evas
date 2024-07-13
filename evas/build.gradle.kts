import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlinx.atomicfu")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    `maven-publish`
}

kotlin {
    jvm()
    androidTarget()

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

    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    mingwX64()

    js(IR)

    @OptIn(ExperimentalWasmDsl::class)
    run {
        wasmJs()
        wasmWasi()
    }

    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC")
    }
}
