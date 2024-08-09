@file:Suppress("unused", "PackageDirectoryMismatch")
package composeValue

import composeValue.UserLoginState.*
import androidx.compose.runtime.Composable
import io.sellmair.evas.State
import io.sellmair.evas.compose.composeValue

//Start
@Composable
fun App() {
    val loginState = UserLoginState.composeValue()
    //                                   ^
    //         Will trigger re-composition if the state changes

    when (loginState) {
        is LoggedOut -> ShowLoginScreen()
        is LoggingIn -> ShowLoginSpinner()
        is LoggedIn -> ShowMainScreen()
        null -> Unit
    }
}
//End

sealed class UserLoginState : State {
    companion object Key : State.Key<UserLoginState?> {
        override val default: UserLoginState? = null
    }

    data object LoggedOut : UserLoginState()
    data object LoggingIn : UserLoginState()
    data object LoggedIn : UserLoginState()

}

@Composable
fun ShowLoginScreen() {

}

@Composable
fun ShowLoginSpinner() {

}

@Composable
fun ShowMainScreen() {

}