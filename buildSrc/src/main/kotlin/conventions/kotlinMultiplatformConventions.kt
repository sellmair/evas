package conventions

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
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
                if (!project.path.startsWith(":samples")) {
                    explicitApi()
                }
            }
        }

        /* Tests */
        tasks.withType<AbstractTestTask>().configureEach {
            outputs.upToDateWhen { false }
            testLogging {
                showStandardStreams = true
                events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            }
        }
    }
}