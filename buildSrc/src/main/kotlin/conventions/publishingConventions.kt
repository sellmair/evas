package conventions

import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.withType

internal fun Project.publishingConventions() {
    plugins.withType<MavenPublishPlugin>().configureEach {
        /* Publish locally to the 'build/repository' folder. Can be useful to check publication issues locally */
        extensions.configure<PublishingExtension> {
            repositories {
                maven(rootDir.resolve("build/repository")) {
                    name = "local"
                }
            }

            /* Publish to GitHub packages */
            repositories {
                maven("https://maven.pkg.github.com/sellmair/evas") {
                    name = "github"
                    credentials {
                        username = providers.gradleProperty("evas.github.user").orNull
                        password = providers.gradleProperty("evas.github.token").orNull
                    }
                }
            }
        }
    }

    /* Publish to maven central */
    plugins.withType<com.vanniktech.maven.publish.MavenPublishPlugin>().all {
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
            configure(KotlinMultiplatform(sourcesJar = true))

            pom {
                name = "Evas"
                inceptionYear = "2024"
                url = "https://github.com/sellmair/evas"
                description = provider { project.description }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                scm {
                    url = "https://github.com/sellmair/evas"
                }

                developers {
                    developer {
                        id = "sellmair"
                        name = "Sebastian Sellmair"
                        email = "sebastian@sellmair.io"
                    }
                }
            }
        }
    }
}