@file:Suppress("unused")

import io.sellmair.evas.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//Start
fun CoroutineScope.launchClickCounterPrinter() = launch {
    ClickCounterState.collect { state ->
        println("Click Count: ${state.count}")
    }
}
//End