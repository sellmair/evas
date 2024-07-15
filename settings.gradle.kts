@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.LibraryExtension
import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

rootProject.name = "events and states"

/*
Configure Plugins
 */
pluginManagement {
    plugins {
        kotlin("plugin.compose") version "2.0.0"
        id("org.jetbrains.kotlinx.atomicfu") version "0.25.0"
        id("org.jetbrains.compose") version "1.6.11"
        id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.15.1"

    }

    repositories {
        mavenCentral()
    }
}


buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin:2.0.0"))
        classpath("com.android.tools.build:gradle:8.5.1")
        classpath("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.15.1")
    }

    repositories {
        google {
            mavenContent {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
    }
}


/*
Configure Repositories / Dependencies
 */
dependencyResolutionManagement {
    repositories {
        repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

        mavenCentral()
        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*androidx.*")
            }
        }
    }
}

/*
Declare subprojects
 */
include(":evas")
include(":evas-compose")
include(":samples:joke-app")
include(":samples:login-screen")


/* Default Kotlin Settings */
gradle.lifecycle.beforeProject {
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
    }
}

/*
Default Android Settings
 */
gradle.lifecycle.beforeProject {
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk = 34
            defaultConfig {
                minSdk = 15
                namespace = "io.sellmair.${project.name}"
            }
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget().publishLibraryVariants("release")
        }
    }
}

/*
Binary Compatibility Validation
 */
gradle.lifecycle.beforeProject {
    plugins.withId("org.jetbrains.kotlinx.binary-compatibility-validator") {
        extensions.configure<kotlinx.validation.ApiValidationExtension> {
            @OptIn(ExperimentalBCVApi::class)
            klib {
                enabled = true
                strictValidation = true
            }
        }
    }
}

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
}
