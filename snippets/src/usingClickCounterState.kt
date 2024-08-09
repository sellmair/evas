@file:Suppress("unused")

import io.sellmair.evas.flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//Start
fun CoroutineScope.launchClickCounterPrinter() = launch {
    ClickCounterState.flow().collect { state ->
        println("Click Count: ${state.count}")
    }
}
//End