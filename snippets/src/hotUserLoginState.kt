@file:Suppress("unused")

import io.sellmair.evas.*
import kotlinx.coroutines.CoroutineScope

//Start

/**
 * Defining an imaginary [UserLoginState] which knows about if the User is currently logged into our
 * application (or currently attempting to)
 *
 * In this example 'null' is chosen as the default state, representing that it is "unknown"
 */
sealed class UserLoginState : State {
    companion object Key : State.Key<UserLoginState?> {
        override val default: UserLoginState? = null
    }

    data object LoggedOut : UserLoginState()
    data object LoggingIn : UserLoginState()
    data class LoggedIn(val userId: UserId) : UserLoginState()
}

/**
 * Launching the [UserLoginState] producing coroutine:
 * This coroutine will:
 * - Try to find the currently logged-in user data from a local database
 * - Handle [LoginRequest] events and tries to log a user in, if received
 */
fun CoroutineScope.launchUserLoginState() = launchState(UserLoginState) {
    val user = getUserFromDatabase()
    if (user != null) {
        LoggedIn(user.userId).emit()
        return@launchState
    }

    /**
     * Oh, oh: User wasn't found in the local database:
     * We're setting the state to [LoggedOut]
     */
    LoggedOut.emit()

    /**
     * From here on, we collect all [LoginRequest] events and try to log the user in, by hitting
     * the network.
     */
    collectEvents<LoginRequest> { request ->
        LoggingIn.emit()

        val response = sendLoginRequestToServer(request.user, request.password)
        if (response.isSuccess) {
            LoggedIn(response.userId).emit()
        } else {
            LoggedOut.emit()
        }
    }
}
//End

private data class LoggedIn(val userId: UserId) : Event
private data object LoggedOut : Event
private data object LoggingIn : Event
data class UserId(val id: String)
data class User(val userId: UserId)
data class LoginRequest(val user: String, val password: String) : Event

private fun getUserFromDatabase(): User? = null
private data class LoginResponse(val isSuccess: Boolean, val userId: UserId)

private fun sendLoginRequestToServer(user: String, password: String): LoginResponse {
    error("Not implemented")
}