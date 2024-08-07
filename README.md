# Evas: **Ev**ents **a**nd **S**tates for Kotlin

<p>
<img src=".img/banner.png" width="512" align="middle"
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

## Dependencies

```kotlin
implementation("io.sellmair:evas:1.0.0-beta01")
```

(Compose Extensions)
```kotlin
implementation("io.sellmair:evas-compose:1.0.0-beta01")
```
---

# Simple Usage
## Send and Subscribe to Events
```kotlin
object LogoutEvent: Event

data class LoginEvent(
    val userName: String, 
    val token: String
): Event

suspend fun listenForLogout() = collectEvents<LogoutEvent> {
    println("User logged out")
}

suspend fun listenForLogin() = collectEvents<LoginEvent> { event -> 
    println("User: ${event.userName} logged in")
}

suspend fun login(userName: String, password: String) {
    val token = httpClient().login(userName, password) ?: return
    LoginEvent(userName, token).emit()
}

suspend fun logout() {
    deleteUserData()
    LogoutEvent.emit()
}

```

## Launch State Producer & Show Compose UI
### Define a 'hot' state and 'hot' state producer
 ```kotlin
 // Define the state
 sealed class UserLoginState: State {
     companion object Key<UserLoginState>: State.Key {
         val default = LoggedOut
     }

     data object LoggedOut: UserLoginState()
     data object LoggingIn: UserLoginState()
     data class LoggedIn(val userId: UserId): UserLoginState()
 }

 // Define the state producer
 fun CoroutineScope.launchUserLoginState() = launchState(UserLoginState) {
     val user = getUserFromDatabase()
     if(user!=null) {
         LoggedIn(user.userId).emit()
         return@launchState
     }

     LoggedOut.emit()

     collectEvents<LoginRequest>() { request ->
         LoggingIn.emit()

         val response = sendLoginRequestToServer(request.user, request.password)
         if(response.isSuccess) {
             LoggedIn(response.userId).emit()
         } else {
             LoggedOut.emit()
         }
     }
 }
 ```

 ### Use State in UI development (e.g., compose, using `io.sellmair:evas-compose`)
 ```kotlin
 @Composable
 fun LoginScreen() {
     val loginState = UserLoginState.composeValue()
     //                                   ^
     //         Will trigger re-composition if the state changes

     when(loginState) {
         is LoggedOut -> // ...
         is LoggingIn -> // ...
         is LoggedIn -> // ...
     }
 }
 ```

___

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