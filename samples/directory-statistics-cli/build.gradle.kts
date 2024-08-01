import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    macosArm64()
    macosX64()
    linuxX64()

    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(deps.okio)
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.executable {
            entryPoint("main")
        }
    }
}