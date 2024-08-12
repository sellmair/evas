plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    `evas-snippets`
}

kotlin {
    jvm()

    macosArm64()
    macosX64()

    linuxX64()
    linuxArm64()

    iosSimulatorArm64()
    iosArm64()
    iosX64()

    sourceSets.commonMain.get().kotlin.setSrcDirs(listOf(files("src")))

    sourceSets.commonMain.dependencies {
        implementation(project(":evas"))
        implementation(project(":evas-compose"))
        implementation(compose.runtime)
        implementation(deps.coroutines.core)
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                finalizedBy(tasks.updateSnippets)
            }
        }
    }
}

