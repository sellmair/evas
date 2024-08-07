package io.sellmair.jokes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvents
import io.sellmair.evas.compose.installStates
import io.sellmair.jokes.ui.MainPage
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val events = Events()
        val states = States()

        lifecycleScope.launch(events + states) {
            launchStates()
        }

        setContent {
            installEvents(events) {
                installStates(states) {
                    MainPage()
                }
            }
        }
    }
}