package io.sellmair.sample

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.sellmair.evas.State
import io.sellmair.evas.collectEventsAsync
import io.sellmair.evas.launchStateProducer
import io.sellmair.sample.loginScreen.UserLoginEvent
import io.sellmair.sample.loginScreen.UserLogoutEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

sealed class UserState : State {
    companion object Key : State.Key<UserState?> {
        override val default: UserState? = null
    }

    data object NotLoggedIn : UserState()
    data class LoggedIn(val email: String, val token: String) : UserState()
}


fun CoroutineScope.launchUserStateActor() = launchStateProducer(UserState, Dispatchers.Main.immediate) {
    val settings = Settings()
    val userEmail = settings.getStringOrNull("user.email")
    val userToken = settings.getStringOrNull("user.token")

    if (userEmail != null && userToken != null) {
        UserState.LoggedIn(userEmail, userToken).emit()
    } else {
        UserState.NotLoggedIn.emit()
    }

    collectEventsAsync<UserLoginEvent> { event ->
        settings["user.email"] = event.email
        settings["user.token"] = event.token
        UserState.LoggedIn(event.email, event.token).emit()
    }

    collectEventsAsync<UserLogoutEvent> {
        settings.remove("user.email")
        settings.remove("user.token")
        UserState.NotLoggedIn.emit()
    }
}
