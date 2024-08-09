@file:Suppress("unused")

import io.sellmair.evas.Events
import io.sellmair.evas.States
import kotlinx.coroutines.withContext

suspend fun snippet() {
    //Start
    val events = Events() // <- create new instance
    val states = States() // <- create new instance
    withContext(events + states) {

    }
    //End
}