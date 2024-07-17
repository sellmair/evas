dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("../dependencies.toml"))
        }
    }
}