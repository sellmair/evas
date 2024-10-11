package io.sellmair.evas.compose.gradle

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

enum class EvasUsage {
    Main, UI,
}

internal val EVAS_USAGE_ATTRIBUTE = Attribute.of(EvasUsage::class.java)


internal fun KotlinCompilation<*>.createUIElementsConfigurations() {
    project.configurations.create("${target.name}${name.capitalized}ApiElements") {
        isCanBeConsumed = true
        isCanBeResolved = false

        extendsFrom(project.configurations.getByName(apiConfigurationName))

        attributes {
            attribute(KotlinPlatformType.attribute, target.platformType)
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_API))
            attribute(EVAS_USAGE_ATTRIBUTE, EvasUsage.UI)
        }

        outgoing.artifact(project.provider { output.classesDirs.singleFile }) {
            builtBy(output.classesDirs)
        }
    }

    project.configurations.create("${target.name}${name.capitalized}RuntimeElements") {
        isCanBeConsumed = true
        isCanBeResolved = false

        val runtimeConfiguration = runtimeDependencyConfigurationName?.let { project.configurations.getByName(it) }
        if (runtimeConfiguration != null) extendsFrom(runtimeConfiguration)

        attributes {
            attribute(KotlinPlatformType.attribute, target.platformType)
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
            attribute(EVAS_USAGE_ATTRIBUTE, EvasUsage.UI)
        }

        outgoing.artifact(project.provider { output.classesDirs.singleFile }) {
            builtBy(output.classesDirs)
        }

        outgoing.artifact(project.provider { output.resourcesDirProvider })
    }
}

