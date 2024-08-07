# Evas: **Ev**ents **a**nd **S**tates for Kotlin

<p>
<img src=".img/banner.png" width="512"
alt="Evas logo by Sebastian Sellmair">
</p>

## Overview

- ✅ Multiplatform (jvm, android, iOS, watchOS, macOS, linux, windows, wasm, js, ...)
- ✅ Fast / Performance Benchmarked (kotlinx.benchmark)
- ✅ Concurrency tested (kotlinx.lincheck)
- ✅ API surface tested (kotlinx.binary-compatibility-validator)
- ✅ Tiny Binary Size (~ 90kb)
- ➕ Compose Extensions
- ➕ Inline documentation with 'usage examples'

---

## Use

```kotlin
implementation("io.sellmair:evas:1.0.0-alpha05")
```

(Compose Extensions)
```kotlin
implementation("io.sellmair:evas-compose:1.0.0-alpha05")
```

---

# Sample Projects
## Login Screen App (iOS, Android, Desktop App)
- [Entry Point: Android](samples/login-screen/src/androidMain/kotlin/io/sellmair/sample/MainActivity.kt)
- [Entry Point: iOS]()

## Joke App (iOS, Android, Desktop App)
- [Entry Point: Android](samples/joke-app/src/androidMain/kotlin/io/sellmair/jokes/MainActivity.kt)
- [Entry Point: iOS](samples/login-screen/src/iosMain/kotlin/io/sellmair/sample/SampleAppViewController.kt)
- [Entry Point: Desktop](samples/login-screen/src/jvmMain/kotlin/io/sellmair/sample/SampleApplication.kt)
- [Login Screen: Compose UI](samples/login-screen/src/commonMain/kotlin/io/sellmair/sample/ui/LoginScreen.kt)

## CLI Application (Directory Scanner)
![directory-statistics-cli.gif](samples/directory-statistics-cli/.img/directory-statistics-cli.gif)
- [Entry Point: Main.kt](https://github.com/sellmair/evas/blob/895fcb39528ff008bcbbe5959b3f79298caabbdc/samples/directory-statistics-cli/src/nativeMain/kotlin/Main.kt)
- [SummaryState](samples/directory-statistics-cli/src/nativeMain/kotlin/SummaryState.kt)
- [Command Line UI](samples/directory-statistics-cli/src/nativeMain/kotlin/uiActor.kt)