package io.sellmair.evas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName

public class StateProducerScope<T : State?> internal constructor(
    private val scope: ProducerScope<T>
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    /**
     * Will emit [value] as the current state
     */
    @JvmName("emitState")
    public suspend fun emit(value: T): Unit = scope.channel.send(value)

    /**
     * Will emit [T] as the current state
     */
    public suspend fun T.emit(): Unit = scope.channel.send(this)

    /**
     * Will emit all values of [flow] as state
     */
    public suspend infix fun emitAll(flow: Flow<T>) {
        flow.collect { element -> scope.channel.send(element) }
    }
}

internal fun <T : State?> stateProducerFlow(block: suspend StateProducerScope<T>.() -> Unit): Flow<T> {
    return channelFlow {
        with(StateProducerScope(this)) {
            block()
        }
    }
}

