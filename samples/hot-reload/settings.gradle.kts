pluginManagement {
    includeBuild("../..")
}

includeBuild("../..")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(":app")
include(":widgets")