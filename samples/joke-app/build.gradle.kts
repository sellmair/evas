import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family.IOS

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    val ktorClientVersion = "2.3.12"
    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(project(":evas-compose"))

        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)

        implementation("io.ktor:ktor-client-core:$ktorClientVersion")
    }

    sourceSets.androidMain.dependencies {
        implementation("androidx.activity:activity-compose:1.9.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("io.ktor:ktor-client-cio:$ktorClientVersion")
    }

    sourceSets.jvmMain.dependencies {
        implementation(compose.desktop.currentOs)
        implementation("io.ktor:ktor-client-cio:$ktorClientVersion")
    }

    sourceSets.appleMain.dependencies {
        implementation("io.ktor:ktor-client-darwin:$ktorClientVersion")
    }
}

/* Android Options */
android {
    compileSdk = 34
    namespace = "io.sellmair.jokes"
    defaultConfig {
        minSdk = 24
        applicationId = "io.sellmair.jokes"
    }
}

/* Desktop Options */
compose.desktop.application {
    mainClass = "io.sellmair.jokes.MainKt"
}

/* iOS options */
kotlin {
    targets.withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family == IOS }
        .configureEach {
            binaries.framework {
                baseName = "JokesKt"
                isStatic = true
            }
        }
}