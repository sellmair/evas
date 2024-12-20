package conventions

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.androidLibraryConventions() {
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk = 34
            defaultConfig {
                minSdk = 15
                namespace = "io.sellmair.${project.name}"
            }

            compileOptions {
                this.sourceCompatibility = JavaVersion.VERSION_11
                this.targetCompatibility = JavaVersion.VERSION_11
            }
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget().publishLibraryVariants("release")
        }
    }
}
