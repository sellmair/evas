import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlinx.atomicfu")
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.kotlinx.benchmark
    `evas-publish`
}

description = "Events and States Library for Kotlin (Multiplatform)"

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
        implementation(deps.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(deps.coroutines.test)
    }
}


/* Configure Benchmarking */
run {
    kotlin.jvm().compilations.create("benchmark") {
        associateWith(kotlin.jvm().compilations.getByName("main"))
    }

    kotlin.sourceSets.getByName("jvmBenchmark").dependencies {
        implementation(deps.kotlinxBenchmarkRuntime)
    }

    benchmark {
        targets {
            register("jvmBenchmark")
        }

        configurations {
            register("events") {
                include(".*\\.events\\..*")
            }

            register("states") {
                include(".*\\.states\\..*")
            }
        }
    }
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            maxHeapSize = "3G"
            maxParallelForks = 12
        }
    }
}