plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
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

dependencies {
    implementation(gradleKotlinDsl())

    /* JetBrains */
    implementation(kotlin("gradle-plugin:${deps.versions.kotlin.get()}"))
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${deps.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:${deps.versions.compose.get()}")
    implementation("org.jetbrains.kotlinx.atomicfu:org.jetbrains.kotlinx.atomicfu.gradle.plugin:0.25.0")
    implementation("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.15.1")
    implementation("org.jetbrains.kotlinx.benchmark:org.jetbrains.kotlinx.benchmark.gradle.plugin:${deps.versions.kotlinxBenchmark.get()}")

    implementation("com.android.tools.build:gradle:${deps.versions.androidGradlePlugin.get()}")
    implementation("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.29.0")
}

gradlePlugin.plugins.create("evas") {
    id = "evas-build"
    implementationClass = "EvasBuildPlugin"
}

gradlePlugin.plugins.create("evas-publish") {
    id = "evas-publish"
    implementationClass = "EvasPublishPlugin"
}

gradlePlugin.plugins.create("evas-snippets") {
    id = "evas-snippets"
    implementationClass = "EvasSnippetsPlugin"
}
