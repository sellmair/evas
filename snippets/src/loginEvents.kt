@file:Suppress("unused", "UNUSED_PARAMETER")

import io.sellmair.evas.Event
import io.sellmair.evas.collectEvents
import io.sellmair.evas.emit

//Start
object LogoutEvent: Event

data class LoginEvent(val userName: String, val token: String): Event

suspend fun listenForLogout() = collectEvents<LogoutEvent> {
    println("User logged out")
}

suspend fun listenForLogin() = collectEvents<LoginEvent> { event ->
    println("User: ${event.userName} logged in")
}

suspend fun login(userName: String, password: String) {
    val token = httpClient().login(userName, password) ?: return
    LoginEvent(userName, token).emit()
                    //          ^
                    // Actually emit the event and suspend until
                    // All listeners have finished processing this event
}

suspend fun logout() {
    deleteUserData()
    LogoutEvent.emit()
    //          ^
    // Actually emit the event and suspend until
    // All listeners have finished processing this event
}
//End

private fun httpClient() = object {
    fun login(userName: String, password: String): String? = null
}

private fun deleteUserData() = Unit