import conventions.androidConventions
import conventions.binaryCompatibilityValidatorConventions
import conventions.kotlinMultiplatformConventions
import org.gradle.api.Plugin
import org.gradle.api.Project

class EvasBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        version = providers.gradleProperty("evas.version").get()
        group = "io.sellmair"

        target.kotlinMultiplatformConventions()
        target.androidConventions()
        target.binaryCompatibilityValidatorConventions()
    }
}
