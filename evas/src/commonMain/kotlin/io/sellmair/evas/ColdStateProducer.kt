package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration

public typealias ColdStateProducer<K, T> = suspend StateProducerScope<T>.(key: K) -> Unit

public inline fun <reified K : State.Key<T>, T : State?> CoroutineScope.launchStateProducer(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    keepActive: Duration = Duration.ZERO,
    noinline onInactive: suspend StateProducerScope<T>.(key: K) -> Unit = OnInactive.resetValue(),
    noinline produce: ColdStateProducer<K, T>
): Job = launchStateProducer(
    coroutineContext = coroutineContext,
    keepActive = keepActive,
    onInactive = onInactive,
    produce = produce,
    keyClazz = K::class
)

@PublishedApi
internal fun <K : State.Key<T>, T : State?> CoroutineScope.launchStateProducer(
    coroutineContext: CoroutineContext,
    keepActive: Duration,
    onInactive: ColdStateProducer<K, T>,
    produce: ColdStateProducer<K, T>,
    keyClazz: KClass<K>
): Job {
    val newCoroutineContext = (this.coroutineContext + coroutineContext).let { base -> base + Job(base.job) }
    val coroutineScope = CoroutineScope(newCoroutineContext)
    val producer = ColdStateProducerImpl(
        coroutineScope = coroutineScope,
        keyClazz = keyClazz,
        keepActive = keepActive,
        onInactive = onInactive,
        onActive = produce,
    )
    newCoroutineContext.statesOrThrow.internal.registerProducer(producer)

    /* Unregister if the job was canceled */
    newCoroutineContext.job.invokeOnCompletion {
        newCoroutineContext.statesOrThrow.internal.unregisterProducer(producer)
    }

    return newCoroutineContext.job
}

@PublishedApi
internal class ColdStateProducerImpl<K : State.Key<T>, T : State?>(
    private val coroutineScope: CoroutineScope,
    private val keyClazz: KClass<K>,
    private val keepActive: Duration = Duration.ZERO,
    private val onInactive: ColdStateProducer<K, T>,
    private val onActive: ColdStateProducer<K, T>
) : StateProducer {
    @Suppress("UNCHECKED_CAST")
    override fun <X : State?> launchIfApplicable(key: State.Key<X>, state: MutableStateFlow<X>) {
        if (!keyClazz.isInstance(key)) return
        key as K
        state as MutableStateFlow<T>
        coroutineScope.launch { produceState(key, state) }
    }

    @OptIn(FlowPreview::class)
    private suspend fun produceState(key: K, state: MutableStateFlow<T>) {
        state.subscriptionCount
            .map { subscriptionCount -> subscriptionCount > 0 }
            .distinctUntilChanged()
            .debounce { isSubscribed -> if (!isSubscribed) keepActive else Duration.ZERO }
            .distinctUntilChanged()
            .collectLatest { isActive ->
                if (isActive && coroutineContext.isActive) {
                    state.emitAll(stateProducerFlow { onActive(key) })
                }

                if (!isActive) {
                    state.emitAll(stateProducerFlow { onInactive(key) })
                }
            }
    }
}