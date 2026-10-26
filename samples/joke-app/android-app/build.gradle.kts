plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

android {
    namespace = "io.sellmair.jokes"
    defaultConfig {
        applicationId = "io.sellmair.jokes"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":samples:joke-app"))
    implementation(project(":evas"))
    implementation(project(":evas-compose"))
    implementation(deps.coroutines.core)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.core:core-ktx:1.19.1")
}
