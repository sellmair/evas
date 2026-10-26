@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "events and states"


/*
Configure Repositories / Dependencies
 */
dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("dependencies.toml"))
        }
    }

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
include(":snippets")
include(":samples:joke-app")
include(":samples:joke-app:android-app")
include(":samples:login-screen")
include(":samples:login-screen:android-app")
include(":samples:directory-statistics-cli")


gradle.lifecycle.beforeProject {
    plugins.apply("evas-build")
}