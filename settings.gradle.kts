@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.LibraryExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost.Companion.CENTRAL_PORTAL
import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.wasm.ir.WasmDataMode
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.JReleaserPlugin
import org.jreleaser.model.Active
import org.jreleaser.model.Signing

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
        classpath("org.jreleaser:jreleaser-gradle-plugin:1.13.1")
        classpath("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.29.0")
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
        gradlePluginPortal()
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
    version = providers.gradleProperty("evas.version").get()
    group = "io.sellmair"

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
            publishToMavenCentral(CENTRAL_PORTAL)
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
