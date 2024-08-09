@file:Suppress("unused")

import io.sellmair.evas.*
import kotlinx.coroutines.CoroutineScope

//Start
data class ClickCounterState(val count: Int) : State {
    /*
    Using the 'companion object' as Key, defining '0' as default state
     */
    companion object Key : State.Key<ClickCounterState> {
        override val default: ClickCounterState = ClickCounterState(count = 0)
    }
}

fun CoroutineScope.launchClickCounterState() = launchState(ClickCounterState) {
    var count = 0
    collectEvents<ClickEvent> {
        count++
        ClickCounterState(count).emit()
        //                        ^
        //                 Emit State Update
    }
}

suspend fun onClick() {
    ClickEvent.emit()
    //          ^
    // emit event and wait for all listening coroutines to finish
}
//End

object ClickEvent : Event