package io.sellmair.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvents
import io.sellmair.evas.compose.installStates
import io.sellmair.sample.ui.App
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val events = Events()
        val states = States()

        lifecycleScope.launch(events + states) {
            launchAppStates()
        }

        setContent {
            installEvents(events) {
                installStates(states) {
                    App()
                }
            }
        }
    }
}