@file:Suppress("unused", "UNUSED_PARAMETER")

import io.sellmair.evas.Event
import io.sellmair.evas.collectEvents
import io.sellmair.evas.emit

//Start
object LogoutEvent: Event

data class LoginEvent(val userName: String, val token: String): Event

/**
 * Use 'collectEvents' to subscribe to all events of type [LogoutEvent]
 */
suspend fun listenForLogout() = collectEvents<LogoutEvent> {
    println("User logged out")
}

/**
 * Use 'collectEvents' to subscribe to all events of type [LoginEvent]
 */
suspend fun listenForLogin() = collectEvents<LoginEvent> { event ->
    println("User: ${event.userName} logged in")
}

/**
 * Example function 'login' which will pretend to login a user and then
 * emits a [LoginEvent]
 */
suspend fun login(userName: String, password: String) {
    val token = httpClient().login(userName, password) ?: return
    LoginEvent(userName, token).emit()
                    //          ^
                    // emit the event and suspend until
                    // All listeners have finished processing this event
}

/**
 * Example function 'logout' which will pretend to delete user data and then
 * emits a [LogoutEvent]
 */
suspend fun logout() {
    deleteUserData()
    LogoutEvent.emit()
    //          ^
    // emit the event and suspend until
    // All listeners have finished processing this event
}
//End

private fun httpClient() = object {
    fun login(userName: String, password: String): String? = null
}

private fun deleteUserData() = Unit