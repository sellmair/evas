package conventions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val androidCompileSdk = 37

fun Project.androidConventions() {
    plugins.withId("com.android.kotlin.multiplatform.library") {
        extensions.configure<KotlinMultiplatformExtension> {
            targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                compileSdk = androidCompileSdk
                minSdk = 15
                namespace = "io.sellmair.${project.name.replace("-", ".")}"
            }
        }
    }

    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            compileSdk = androidCompileSdk
            defaultConfig {
                minSdk = 24
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}
