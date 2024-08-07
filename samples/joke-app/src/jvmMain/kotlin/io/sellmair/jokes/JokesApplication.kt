package io.sellmair.jokes

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvents
import io.sellmair.evas.compose.installStates
import io.sellmair.jokes.ui.MainPage

fun main() = application {
    val events = Events()
    val states = States()

    rememberCoroutineScope { events + states }
        .launchStates()

    installEvents(events) {
        installStates(states) {
            Window(
                title = "Jokes",
                onCloseRequest = ::exitApplication,
            ) {
                MainPage()
            }
        }
    }
}