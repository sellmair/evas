tasks.register<Delete>("clean") {
    delete(file("build"))
}