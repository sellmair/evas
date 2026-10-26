import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family.IOS

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(21)

    /* Supported Targets */
    jvm()
    android {}
    iosArm64()
    iosSimulatorArm64()

    /* Dependencies */
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
        implementation(deps.ktorClientCio)
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
