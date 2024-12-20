package conventions

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.kotlinMultiplatformConventions() {
    plugins.withType<KotlinMultiplatformPluginWrapper>().all {
        extensions.configure<KotlinMultiplatformExtension> {

            /*
            Jvm options
             */
            jvmToolchain(21)

            /*
            Kotlin Compiler Options
             */
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                if (!project.path.startsWith(":samples") && !project.path.startsWith(":snippets")) {
                    explicitApi()
                }
            }

            tasks.withType<KotlinJvmCompile>().configureEach {
                compilerOptions {
                    this.jvmTarget.set(JvmTarget.JVM_11)
                    this.freeCompilerArgs.add("-Xjdk-release=11")
                }
            }

            tasks.withType<JavaCompile>().configureEach {
                sourceCompatibility = "11"
                targetCompatibility = "11"
            }
        }

        /* Tests */
        tasks.withType<AbstractTestTask>().configureEach {
            outputs.upToDateWhen { false }
            if (this is Test) {
                maxHeapSize = "4G"
            }
            testLogging {
                showStandardStreams = true
                events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            }
        }
    }
}
