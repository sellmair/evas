import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("io.sellmair.evas-compose")
}

kotlin {
    jvmToolchain(17)
    jvm()

    sourceSets.jvmMain.dependencies {
        implementation("io.sellmair:evas:1.2.0-SNAPSHOT")
        implementation("io.sellmair:evas-compose:1.2.0-SNAPSHOT")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        implementation(compose.desktop.currentOs)
        implementation(compose.foundation)
        implementation(compose.material3)
    }
}
