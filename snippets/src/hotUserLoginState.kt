@file:Suppress("unused")

import io.sellmair.evas.*
import kotlinx.coroutines.CoroutineScope

//Start
/*
Defining the State
 */
sealed class UserLoginState : State {
    companion object Key : State.Key<UserLoginState> {
        override val default = LoggedOut
    }

    data object LoggedOut : UserLoginState()
    data object LoggingIn : UserLoginState()
    data class LoggedIn(val userId: UserId) : UserLoginState()
}

/*
Launch the 'State producing coroutine'
 */
fun CoroutineScope.launchUserLoginState() = launchState(UserLoginState) {
    val user = getUserFromDatabase()
    if (user != null) {
        LoggedIn(user.userId).emit()
        return@launchState
    }

    LoggedOut.emit()

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