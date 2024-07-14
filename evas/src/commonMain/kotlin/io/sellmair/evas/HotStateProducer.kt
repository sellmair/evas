package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public fun <T : State?> CoroutineScope.launchStateProducer(
    key: State.Key<T>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    started: SharingStarted = SharingStarted.Eagerly,
    produce: suspend StateProducerScope<T>.() -> Unit
): Job {
    val newCoroutineContext = (this.coroutineContext + coroutineContext).let { base -> base + Job(base.job) }
    val coroutineScope = CoroutineScope(newCoroutineContext)

    val hotFlow = stateProducerFlow(produce).shareIn(coroutineScope, started, replay = 1)

    coroutineScope.launch {
        currentCoroutineContext().statesOrThrow.setState(key, hotFlow)
    }

    return coroutineScope.coroutineContext.job
}



