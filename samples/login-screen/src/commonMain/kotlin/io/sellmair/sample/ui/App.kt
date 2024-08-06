package io.sellmair.sample.ui

import androidx.compose.runtime.Composable
import io.sellmair.evas.compose.composeValue
import io.sellmair.sample.UserState

@Composable
fun App() {
    when (UserState.composeValue() ?: return) {
        is UserState.NotLoggedIn -> LoginScreen()
        is UserState.LoggedIn -> MainScreen()
    }
}
