package io.sellmair.sample.loginScreen

import io.sellmair.evas.*
import io.sellmair.sample.loginScreen.network.Backend
import io.sellmair.sample.loginScreen.network.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

sealed class UserLoginState : State {
    companion object Key : State.Key<UserLoginState?> {
        override val default: UserLoginState? = null
    }

    data class NotLoggedIn(val error: String? = null) : UserLoginState()
    data object LoggingIn : UserLoginState()
    data class LoggedIn(val email: String, val token: String) : UserLoginState()
}

fun CoroutineScope.launchUserLoginStateActor() = launchStateProducer(UserLoginState, Dispatchers.Main.immediate) {
    val passwordStateFlow = events<PasswordChangedEvent>().map { it.password }
        .stateIn(this, SharingStarted.Eagerly, null)

    UserLoginState.NotLoggedIn().emit()

    collectEvents<LoginClickedEvent> {
        val emailState = EmailState.get().value
        val password = passwordStateFlow.value

        if (!emailState.isValid) return@collectEvents
        if (password == null) return@collectEvents

        UserLoginState.LoggingIn.emit()

        when (val loginResult = Backend().login(email = emailState.email, password = password)) {
            is LoginResult.Failure -> UserLoginState.NotLoggedIn(loginResult.message).emit()
            is LoginResult.Success -> {
                UserLoginState.LoggedIn(email = emailState.email, token = loginResult.token).emit()
                UserLoginEvent(email = emailState.email, token = loginResult.token).emit()
            }
        }
    }
}
