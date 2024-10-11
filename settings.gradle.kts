@file:Suppress("UnstableApiUsage")


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
include(":evas-compose:gradle-plugin")

include(":snippets")
include(":samples:joke-app")
include(":samples:login-screen")
include(":samples:directory-statistics-cli")


gradle.lifecycle.beforeProject {
    plugins.apply("evas-build")
}