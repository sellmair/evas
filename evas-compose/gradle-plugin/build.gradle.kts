plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    `evas-publish`
}

publishing {
    publications.create<MavenPublication>("plugin") {
        from(components["java"])
    }
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