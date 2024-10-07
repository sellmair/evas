package io.sellmair.evas

import io.sellmair.evas.State.Key
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsName

/**
 * Factory function: Creates a new instance of [States]
 * See: [States]
 * See: [State]
 */
@JsName("createStates")
public fun States(): States = StatesImpl()

/**
 * Container of all [State] objects.
 * A given [State] can be requested, subscribed, or emitted in this [States] object.
 *
 * ## Example Usages
 * ### Installing in the current coroutine context:
 * Whilst it is possible to pass the [States] instance around, using any kind of dependency injection mechanism,
 * an easier and more pragmatic approach would be installing the [States] object in the current coroutine context
 *
 * ```kotlin
 * suspend fun main() {
 *     withContext(States()) {
 *         // ...
 *     }
 * }
 * ```
 *
 * ### Installing in the current composition (using `io.sellmair:evas-compose`)
 * ```kotlin
 * fun App() {
 *      val states = States()
 *      // ...
 *      installStates(states) { // <- Will register the 'states' instance as Composition Local
 *          MainPage() // <- another 'Composable' function
 *      }
 * }
 * ```
 *
 * See: https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal
 *
 * ### Define a 'hot' state and 'hot' state producer
 * ```
 * // Define the state
 * sealed class UserLoginState: State {
 *     companion object Key<UserLoginState>: State.Key {
 *         val default = LoggedOut
 *     }
 *
 *     data object LoggedOut: UserLoginState()
 *     data object LoggingIn: UserLoginState()
 *     data class LoggedIn(val userId: UserId): UserLoginState()
 * }
 *
 * // Define the state producer
 * fun CoroutineScope.launchUserLoginState() = launchState(UserLoginState) {
 *     val user = getUserFromDatabase()
 *     if(user!=null) {
 *         LoggedIn(user.userId).emit()
 *         return@launchState
 *     }
 *
 *     LoggedOut.emit()
 *
 *     collectEvents<LoginRequest>() { request ->
 *         LoggingIn.emit()
 *
 *         val response = sendLoginRequestToServer(request.user, request.password)
 *         if(response.isSuccess) {
 *             LoggedIn(response.userId).emit()
 *         } else {
 *             LoggedOut.emit()
 *         }
 *     }
 * }
 * ```
 *
 * ### Use State in UI development (e.g., compose, using `io.sellmair:evas-compose`)
 * ```kotlin
 * @Composable
 * fun LoginScreen() {
 *     val loginState = UserLoginState.composeValue()
 *     //                                   ^
 *     //         Will trigger re-composition if the state changes
 *
 *     when(loginState) {
 *         is LoggedOut -> // ...
 *         is LoggingIn -> // ...
 *         is LoggedIn -> // ...
 *     }
 * }
 * ```
 */
public sealed interface States : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key

    public class Update<out T> internal constructor(public val previous: T, public val value: T) {

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is Update<*>) return false
            if (other.previous != previous) return false
            if (other.value != value) return false
            return true
        }

        override fun hashCode(): Int {
            return 13 + previous.hashCode() * 31 + value.hashCode()
        }

        override fun toString(): String {
            return "Update(previous=$previous, value=$value)"
        }
    }

    /**
     * Immediately sets the state for the given [key]. All state listeners will be notified.
     * This API is especially useful when writing tests.
     * For most production use cases [launchState] is more suitable.
     */
    public fun <T : State?> setState(key: State.Key<T>, value: T)

    /**
     * Updates the state atomically:
     * ⚠️ Note: If the state is congested, then [update] function can be called several times, see
     * [MutableStateFlow.update]
     *
     * @return An update containing the previous state and the new updated value.
     */
    public fun <T : State?> updateState(key: State.Key<T>, update: (T) -> T): Update<T>

    /**
     * Updates the state atomically:
     * ⚠️Note: If the state is congested (many update requests in parallel), then the [update] function
     * might be called several times, see [MutableStateFlow.update]
     *
     * @return the previous state, which was used to create the new state
     */
    public fun <T : State?> getAndUpdateState(key: State.Key<T>, update: (T) -> T): T

    /**
     * Updates the state atomically:
     * ⚠️: If the state is congested (many update requests in parallel), then the [update] function
     * might be called several times, see [MutableStateFlow.update]
     *
     * @return The new state after the update finished
     */
    public fun <T : State?> updateAndGetState(key: State.Key<T>, update: (T) -> T): T

    /**
     * Will return the state associated by the given [key] as [StateFlow].
     * Note: Subscribing to the returned [StateFlow] will trigger the 'State Producers' if the given
     * state is cold.
     * See [launchState]
     */
    public fun <T : State?> getState(key: State.Key<T>): StateFlow<T>

    /**
     * Will emit all [values] to the state associated with the given [key].
     * Similar to [setState]; Will suspend until all [values] have been emitted.
     */
    public suspend fun <T : State?> setState(key: State.Key<T>, values: Flow<T>)

    public companion object Key : CoroutineContext.Key<States>
}

/**
 * @returns the [States] instance installed in the current coroutine context.
 * @throws MissingStatesException if there is no [States] instance available
 * @see statesOrNull
 */
public val CoroutineContext.statesOrThrow: States
    get() = this[States]
        ?: throw MissingStatesException("Missing ${States::class.simpleName} in coroutine context")

/**
 * @returns the [States] instance installed in the current coroutine context or null.
 * @see statesOrThrow
 */
public val CoroutineContext.statesOrNull: States?
    get() = this[States]

/**
 * @return the state associated with the given [Key] as [StateFlow].
 * @throws [MissingStatesException] if there is no [States] instance installed in the current coroutine context.
 */
public suspend fun <T : State?> Key<T>.flow(): StateFlow<T> {
    return coroutineContext.statesOrThrow.getState(this)
}

public suspend fun <T : State?> Key<T>.value(): T {
    return flow().value
}

/**
 * Shortcut for `get().collect(collector)`
 * See [flow]
 * See [Flow.collect]
 */
public suspend fun <T : State?> Key<T>.collect(collector: FlowCollector<T>) {
    flow().collect(collector)
}


/**
 * See [States.setState]
 * @throws [MissingStatesException] if there is no [States] instance installed in the current coroutine context
 */
public suspend fun <T : State?> Key<T>.set(value: T) {
    return coroutineContext.statesOrThrow.setState(this, value)
}

/**
 * See [States.updateState]
 * @throws MissingStatesException if there is no [States] instance installed in the current corotuine context
 */
public suspend fun <T : State?> Key<T>.update(update: (T) -> T): States.Update<T> {
    return coroutineContext.statesOrThrow.updateState(this, update)
}


/**
 * See [States.updateAndGetState]
 * @throws MissingStatesException if there is no [States] instance installed in the current corotuine context
 */
public suspend fun <T : State?> Key<T>.updateAndGet(update: (T) -> T): T{
    return coroutineContext.statesOrThrow.updateAndGetState(this, update)
}

/**
 * See [States.getAndUpdateState]
 * @throws MissingStatesException if there is no [States] instance installed in the current corotuine context
 */
public suspend fun <T : State?> Key<T>.getAndUpdate(update: (T) -> T): T{
    return coroutineContext.statesOrThrow.getAndUpdateState(this, update)
}

internal val States.internal: StatesImpl
    get() = when (this) {
        is StatesImpl -> this
    }

internal sealed interface StateProducer {
    fun <T : State?> launchIfApplicable(key: Key<T>, state: MutableStateFlow<T>)
}

internal class StatesImpl : States {

    private val lock = reentrantLock()

    private val states = hashMapOf<Key<*>, MutableStateFlow<*>>()

    private val producers = mutableListOf<StateProducer>()

    override fun <T : State?> setState(key: Key<T>, value: T) {
        getOrCreateMutableStateFlow(key).value = value
    }

    override suspend fun <T : State?> setState(key: Key<T>, values: Flow<T>) {
        getOrCreateMutableStateFlow(key).emitAll(values)
    }

    fun registerProducer(producer: StateProducer) = lock.withLock {
        producers.add(producer)

        @Suppress("UNCHECKED_CAST")
        states.forEach { (key, state) ->
            key as Key<State?>
            state as MutableStateFlow<State?>
            producer.launchIfApplicable(key, state)
        }
    }

    fun unregisterProducer(producer: StateProducer) = lock.withLock {
        producers.remove(producer)
    }

    override fun <T : State?> getState(key: Key<T>): StateFlow<T> {
        return getOrCreateMutableStateFlow(key)
    }

    override fun <T : State?> updateState(key: Key<T>, update: (T) -> T): States.Update<T> {
        val state = getOrCreateMutableStateFlow(key)
        while (true) {
            val previous = state.value
            val next = update(previous)
            if (state.compareAndSet(previous, next)) {
                return States.Update(previous = previous, value = next)
            }
        }
    }

    override fun <T : State?> updateAndGetState(key: Key<T>, update: (T) -> T): T {
        return getOrCreateMutableStateFlow(key).updateAndGet(update)
    }

    override fun <T : State?> getAndUpdateState(key: Key<T>, update: (T) -> T): T {
        return getOrCreateMutableStateFlow(key).getAndUpdate(update)
    }

    internal fun <T : State?> getMutableState(key: Key<T>): MutableStateFlow<T> {
        return getOrCreateMutableStateFlow(key)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : State?> getOrCreateMutableStateFlow(key: Key<T>): MutableStateFlow<T> {
        /* fast path: No locking required, flow is already available */
        states[key]?.let { return it as MutableStateFlow<T> }

        return lock.withLock {
            states.getOrPut(key) {
                MutableStateFlow(key.default).also { state ->
                    producers.forEach { producer ->
                        producer.launchIfApplicable(key, state)
                    }
                }
            } as MutableStateFlow<T>
        }
    }
}

