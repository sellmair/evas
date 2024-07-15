package io.sellmair.sample.ui

import androidx.compose.runtime.Composable
import io.sellmair.evas.compose.collectAsValue
import io.sellmair.sample.UserState

@Composable
fun App() {
    when (UserState.collectAsValue() ?: return) {
        is UserState.NotLoggedIn -> LoginScreen()
        is UserState.LoggedIn -> MainScreen()
    }
}
