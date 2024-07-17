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
include(":samples:joke-app")
include(":samples:login-screen")


gradle.lifecycle.beforeProject {
    plugins.apply("evas-build")
}