@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("multiplatform") apply false
}

tasks.register<Delete>("clean") {
    delete(file("build"))
}
