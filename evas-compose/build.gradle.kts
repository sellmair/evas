import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    `evas-publish`
}

description = "Compose (Multiplatform) extensions for evas"

kotlin {
    jvm()

    macosArm64()
    macosX64()

    linuxX64()
    linuxArm64()

    iosSimulatorArm64()
    iosArm64()
    iosX64()

    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(compose.runtime)
        implementation(deps.coroutines.core)
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))

        implementation(compose.material)
        implementation(compose.ui)
        @OptIn(ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
        implementation(compose.foundation)
        implementation(compose.desktop.currentOs)
    }
}

