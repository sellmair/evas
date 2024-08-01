package io.sellmair.evas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


public fun <T : State?> CoroutineScope.launchStateProducer(
    key: State.Key<T>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    started: StateProducerStarted = StateProducerStarted.Eagerly,
    produce: suspend StateProducerScope<T>.() -> Unit
): Job {
    val newCoroutineContext = (this.coroutineContext + coroutineContext)
    val states = newCoroutineContext.statesOrThrow
    val coroutineScope = CoroutineScope(newCoroutineContext)

    return when (started) {
        StateProducerStartedEagerly -> coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            states.setState(key, stateProducerFlow(produce))
        }

        StateProducerStartedLazily -> coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            /* We first await at least one subscriber */
            states.internal.getMutableState(key).subscriptionCount.first { subscriptionsCount ->
                subscriptionsCount > 0
            }

            /* Then we can start emitting the state */
            states.setState(key, stateProducerFlow(produce))
        }
    }
}
