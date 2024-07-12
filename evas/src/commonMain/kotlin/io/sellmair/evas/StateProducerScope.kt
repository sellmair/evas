package io.sellmair.evas

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.experimental.ExperimentalTypeInference

public class StateProducerScope<T : State?> internal constructor(
    private val scope: ProducerScope<T>
)  {
    public suspend fun T.emit(): Unit = scope.channel.send(this)

    public suspend infix fun emitAll(flow: Flow<T>) {
        flow.collect { element -> scope.channel.send(element) }
    }
}

@OptIn(ExperimentalTypeInference::class)
internal fun <T : State?> stateProducerFlow(@BuilderInference block: suspend StateProducerScope<T>.() -> Unit): Flow<T> {
    return channelFlow {
        with(StateProducerScope(this)) {
            block()
        }
    }
}

