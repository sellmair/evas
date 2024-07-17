import conventions.publishingConventions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class EvasPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        plugins.apply(MavenPublishPlugin::class.java)
        plugins.apply(com.vanniktech.maven.publish.MavenPublishPlugin::class.java)

        target.publishingConventions()
    }
}