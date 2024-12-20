package conventions

import com.android.build.gradle.TestedExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.androidConventions() {
    fun configureDefaults() {
        extensions.configure<TestedExtension> {
            setCompileSdkVersion(35)
            defaultConfig {
                minSdk = 15
                namespace = "io.sellmair.${project.name.replace("-", ".")}"
            }

            compileOptions {
                this.sourceCompatibility = JavaVersion.VERSION_11
                this.targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }

    plugins.withId("com.android.library") {
        configureDefaults()

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget().publishLibraryVariants("release")
        }
    }

    plugins.withId("com.android.application") {
        configureDefaults()
    }
}
