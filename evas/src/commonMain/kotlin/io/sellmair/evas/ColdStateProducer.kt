package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration

public typealias ColdStateProducer<K, T> = suspend StateProducerScope<T>.(key: K) -> Unit

/**
 * Launching coroutines, producing [State]s 'cold' for request State keys.
 * Every state launching a coroutine will receive the exact 'requesting' key as input:
 *
 * ## Example usage
 * ### User 'online' status state
 *
 * Definition of the 'State' and the 'Key':
 * Note, the online state of a user requires the key to carry the userId!
 *
 * ```kotlin
 * data class UserOnlineState(val isOnline: Boolean, val lastOnline: Date?): State {
 *     data class Key(val userId: UserId): State.Key<UserOnlineState> {
 *         override val default = UserOnlineState(false, null)
 *     }
 * }
 * ```
 *
 * UI requesting the user's online status
 * ```kotlin
 * @Composable
 * fun UserOnlineStatusBanner(user: UserId) {
 *     val onlineState = UserOnlineState.Key(user).composeValue()
 *                         //             ^
 *                         // Requesting the state for exactly this user
 *
 *     Text(if(onlineState.isOnline) "Online" else "Offline")
 *     if(onlineState.lastOnline != null) Text("Last online: ${onlineState.lastOnline}")
 * }
 * ```
 *
 * launching the State:
 * ```kotlin
 * fun CoroutineScope.launchUserOnlineState() = launchState { key: UserOnlineState.Key ->
 *      var lastOnlineDate: Date? = null
 *      while(isActive) {
 *         //    ^
 *         // Will be active until no more subscribers to this State (key) are present
 *
 *          val isOnline = httpClient.isUserOnline(key.user)
 *          if(isOnline) {
 *              lastOnlineDate = Date.now()
 *          }
 *          UserOnlineState(isOnline, lastOnlineDate).emit()
 *          delay(5.seconds)
 *      }
 * }
 * ```
 *
 * @param keepActive Allows keeping a state-producing coroutine active for the specified period of time
 * after no more subscribers are present
 *
 * @param onInactive Allows specifying what to do once the state-producing coroutine dies
 * - default: [OnInactive.resetValue]: Will reset the state to its default value
 * - option: [OnInactive.keepValue]: Will keep the last state value
 * - option: Provide a coroutine that can emit further states
 */
public inline fun <reified K : State.Key<T>, T : State?> CoroutineScope.launchState(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    keepActive: Duration = Duration.ZERO,
    noinline onInactive: suspend StateProducerScope<T>.(key: K) -> Unit = OnInactive.resetValue(),
    noinline produce: ColdStateProducer<K, T>
): Job = launchState(
    coroutineContext = coroutineContext,
    keepActive = keepActive,
    onInactive = onInactive,
    produce = produce,
    keyClazz = K::class
)

@PublishedApi
internal fun <K : State.Key<T>, T : State?> CoroutineScope.launchState(
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