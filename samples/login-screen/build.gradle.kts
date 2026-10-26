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
    /* Supported Targets */
    jvm()
    android {}
    iosArm64()
    iosSimulatorArm64()

    /* Dependencies */
    val ktorClientVersion = "2.3.12"
    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(project(":evas-compose"))

        implementation("com.russhwolf:multiplatform-settings:1.3.0")
        implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")

        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.materialIconsExtended)

        implementation(deps.ktorClientCore)
        implementation(deps.coroutines.core)
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
        implementation(deps.coroutines.swing)
        implementation(deps.ktorClientCio)
    }

    sourceSets.appleMain.dependencies {
        implementation(deps.ktorClientDarwin)
    }
}

/* Desktop Options */
compose.desktop.application {
    mainClass = "io.sellmair.sample.SampleApplicationKt"
}

kotlin.jvm().mainRun {
    mainClass = "io.sellmair.sample.SampleApplicationKt"
}

/* iOS options */
kotlin {
    targets.withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family == IOS }
        .configureEach {
            binaries.framework {
                baseName = "SampleAppKt"
                isStatic = true
            }
        }
}
