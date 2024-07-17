package conventions

import kotlinx.validation.ExperimentalBCVApi
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.binaryCompatibilityValidatorConventions() =
    plugins.withId("org.jetbrains.kotlinx.binary-compatibility-validator") {
        extensions.configure<kotlinx.validation.ApiValidationExtension> {
            @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
            klib {
                enabled = true
                strictValidation = true
            }
        }
    }
