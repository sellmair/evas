package io.sellmair.jokes

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvents
import io.sellmair.evas.compose.installStates
import io.sellmair.jokes.ui.MainPage

@Suppress("unused") // Used from the iOS project.
fun createViewController() = ComposeUIViewController {
    val events = Events()
    val states = States()

    rememberCoroutineScope { events + states }
        .launchStates()

    installEvents(events) {
        installStates(states) {
            MainPage()
        }
    }
}