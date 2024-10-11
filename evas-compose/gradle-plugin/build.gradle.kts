plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

gradlePlugin {
    plugins.create("evas-compose") {
        id = "io.sellmair.evas-compose"
        implementationClass = "io.sellmair.evas.compose.gradle.EvasComposeGradlePlugin"
    }
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
}