package io.sellmair.sample

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvents
import io.sellmair.evas.compose.installStates
import io.sellmair.sample.ui.App

@Suppress("unused") // Used from the iOS project.
fun createViewController() = ComposeUIViewController {
    val events = Events()
    val states = States()

    rememberCoroutineScope { events + states }
        .launchAppStates()

    installEvents(events) {
        installStates(states) {
            App()
        }
    }
}