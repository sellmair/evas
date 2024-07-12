@file:Suppress("UnstableApiUsage")

/*
Publishing
*/
gradle.lifecycle.beforeProject {
    version = "1.0.0-SNAPSHOT"
    group = "io.sellmair"

    plugins.withType<MavenPublishPlugin>().configureEach {
        extensions.configure<PublishingExtension> {
            repositories {
                maven(rootDir.resolve("build/repository")) {
                    name = "local"
                }
            }
        }
    }
}

/*
Configure Plugins
 */
pluginManagement {
    plugins {
        kotlin("multiplatform") version "2.0.0"
        id("org.jetbrains.kotlinx.atomicfu") version "0.25.0"
    }

    repositories {
        mavenCentral()
    }
}

/*
Configure Repositories / Dependencies
 */
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    }
}

/*
Declare subprojects
 */
include(":core")
