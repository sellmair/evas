import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family.IOS

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(21)

    /* Supported Targets */
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    /* Dependencies */
    val ktorClientVersion = "2.3.12"
    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(project(":evas-compose"))

        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)

        implementation(deps.ktorClientCore)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))

        @OptIn(ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
    }

    sourceSets.androidMain.dependencies {
        implementation("androidx.activity:activity-compose:1.9.2")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("io.ktor:ktor-client-cio:$ktorClientVersion")
    }

    sourceSets.jvmMain.dependencies {
        implementation(compose.desktop.currentOs)
        implementation(deps.ktorClientCio)
        implementation(deps.coroutines.swing)
    }

    sourceSets.appleMain.dependencies {
        implementation(deps.ktorClientDarwin)
    }
}

/* Android Options */
android {
    compileSdk = 34
    namespace = "io.sellmair.jokes"
    defaultConfig {
        minSdk = 24
        applicationId = "io.sellmair.jokes"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

/* Setup Android Tests (For this project I would like them to not be connected to 'commonTest') */
kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.instrumentedTest)
        }

        unitTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.unitTest)
        }
    }
}

/* Desktop Options */
compose.desktop.application {
    mainClass = "io.sellmair.jokes.JokesApplicationKt"
}

kotlin.jvm().mainRun {
    mainClass = "io.sellmair.jokes.JokesApplicationKt"
}

/* iOS options */
kotlin.targets.withType<KotlinNativeTarget>()
    .matching { it.konanTarget.family == IOS }
    .configureEach {
        binaries.framework {
            baseName = "JokesKt"
            isStatic = true
        }
    }

