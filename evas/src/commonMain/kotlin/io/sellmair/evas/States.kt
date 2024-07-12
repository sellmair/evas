package io.sellmair.evas

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

public fun States(): States = StatesImpl()

public sealed interface States : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    public fun <T : State?> setState(key: State.Key<T>, value: T)
    public fun <T : State?> getState(key: State.Key<T>): StateFlow<T>
    public suspend fun <T : State?> setState(key: State.Key<T>, values: Flow<T>)

    public companion object Key : CoroutineContext.Key<States>
}

internal val States.internal: StatesImpl
    get() = when (this) {
        is StatesImpl -> this
    }

internal class StatesImpl : States {

    private val lock = reentrantLock()

    private val states = hashMapOf<State.Key<*>, MutableStateFlow<*>>()

    private val producers = mutableListOf<StateProducer>()

    override fun <T : State?> setState(key: State.Key<T>, value: T) {
        getOrCreateMutableStateFlow(key).value = value
    }

    override suspend fun <T : State?> setState(key: State.Key<T>, values: Flow<T>) {
        getOrCreateMutableStateFlow(key).emitAll(values)
    }

    fun registerProducer(producer: StateProducer) = lock.withLock {
        producers.add(producer)

        @Suppress("UNCHECKED_CAST")
        states.forEach { (key, state) ->
            key as State.Key<State?>
            state as MutableStateFlow<State?>
            producer.launchIfApplicable(key, state)
        }
    }

    fun unregisterProducer(producer: StateProducer) = lock.withLock {
        producers.remove(producer)
    }

    override fun <T : State?> getState(key: State.Key<T>): StateFlow<T> {
        return getOrCreateMutableStateFlow(key).asStateFlow()
    }

    private fun <T : State?> getOrCreateMutableStateFlow(key: State.Key<T>): MutableStateFlow<T> = lock.withLock {
        @Suppress("UNCHECKED_CAST")
        return states.getOrPut(key) {
            MutableStateFlow(key.default).also { state ->
                producers.forEach { producer ->
                    producer.launchIfApplicable(key, state)
                }
            }
        } as MutableStateFlow<T>
    }
}

public val CoroutineContext.states: States
    get() = this[States] ?: error("Missing ${States::class.simpleName}")


public suspend fun <T : State?> State.Key<T>.get(): StateFlow<T> {
    return coroutineContext.states.getState(this)
}

public suspend fun <T : State?> State.Key<T>.set(value: T) {
    return coroutineContext.states.setState(this, value)
}
