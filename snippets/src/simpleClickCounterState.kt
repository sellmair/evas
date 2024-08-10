@file:Suppress("unused")

import io.sellmair.evas.*
import kotlinx.coroutines.CoroutineScope

//Start
/**
 * Pretend: Our application cares about the number of 'clicks' the user has performed.
 * We define our [ClickCounterState] here which holds this information.
 */
data class ClickCounterState(val count: Int) : State {
    /*
    Using the 'companion object' as Key, defining '0' as the default state
     */
    companion object Key : State.Key<ClickCounterState> {
        override val default: ClickCounterState = ClickCounterState(count = 0)
    }
}

/**
 * Launching the coroutine, which will produce the [ClickCounterState].
 * It will collect the [ClickEvent]s and update the [ClickCounterState] by incrementing for each click it receives.
 */
fun CoroutineScope.launchClickCounterState() = launchState(ClickCounterState) {
    var count = 0
    collectEvents<ClickEvent> {
        count++
        ClickCounterState(count).emit()
        //                        ^
        //                 Emit State Update
    }
}

/**
 * Imaginary 'onClick' method which will send the [ClickEvent] to the application
 */
suspend fun onClick() {
    ClickEvent.emit()
    //          ^
    // emit event and wait for all listening coroutines to finish
}
//End

object ClickEvent : Event