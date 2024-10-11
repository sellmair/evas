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

    sourceSets.commonMain.dependencies {
        implementation("io.sellmair:evas")
        implementation("io.sellmair:evas-compose")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        implementation("ch.qos.logback:logback-classic:1.5.9")
        implementation(compose.desktop.currentOs)
        implementation(compose.foundation)
        implementation(compose.material3)


        implementation(project(":widgets"))
    }
}
