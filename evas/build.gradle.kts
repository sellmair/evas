import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    org.jetbrains.kotlinx.atomicfu
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.kotlinx.benchmark
    `evas-publish`
}

description = "Events and States Library for Kotlin (Multiplatform)"

kotlin {
    jvm()
    androidTarget()

    linuxX64()
    linuxArm64()

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
        implementation(deps.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(deps.coroutines.test)
    }

    sourceSets.jvmTest.dependencies {
        implementation(deps.lincheck)
    }
}


/* Configure Benchmarking */
run {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    kotlin.applyDefaultHierarchyTemplate {
        withSourceSetTree(KotlinSourceSetTree("benchmark"))
    }

    kotlin.jvm().compilations.create("benchmark") {
        associateWith(kotlin.jvm().compilations.getByName("main"))
        defaultSourceSet.dependencies {
            implementation("org.greenrobot:eventbus-java:3.3.1")
        }
    }

    kotlin.macosArm64().compilations.create("benchmark") {
        associateWith(kotlin.macosArm64().compilations.getByName("main"))
        /* Let's use 'associateWith' once kotlinx.benchmark also starts using this mechanism */
        defaultSourceSet.dependencies {
            implementation(project)
        }
    }

    kotlin.sourceSets.getByName("commonBenchmark").dependencies {
        implementation(deps.kotlinxBenchmarkRuntime)
    }

    benchmark {
        targets {
            register("jvmBenchmark")
            register("macosArm64Benchmark")
        }

        configurations {
            register("emit") {
                include(".*EmitBenchmark.*")
            }

            register("events") {
                include(".*\\.events\\..*")
            }

            register("states") {
                include(".*\\.states\\..*")
            }

            register("compare") {
                include(".*DifferentLibraryComparisonBenchmark.*")
            }
        }
    }
}

