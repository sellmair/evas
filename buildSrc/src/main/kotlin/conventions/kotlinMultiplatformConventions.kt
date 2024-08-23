package conventions

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

internal fun Project.kotlinMultiplatformConventions() {
    plugins.withType<KotlinMultiplatformPluginWrapper>().all {
        extensions.configure<KotlinMultiplatformExtension> {
            /* Targets */


            /*
            Jvm options
             */
            jvmToolchain(17)

            /*
            Kotlin Compiler Options
             */
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                if (!project.path.startsWith(":samples") && !project.path.startsWith(":snippets")) {
                    explicitApi()
                }
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