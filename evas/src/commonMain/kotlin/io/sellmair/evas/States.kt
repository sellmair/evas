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
 * fun CoroutineScope.launchUserLoginStateActor() = launchStateProducer(UserLoginState) {
 *     val user = getUserFromDatabase()
 *     if(user!=null) {
 *         LoggedIn(user.userId).emit()
 *         return@launchStateProducer
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
 *     val loginState = UserLoginState.collectAsValue()
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

    /**
     * Immediately sets the state for the given [key]. All state listeners will be notified.
     * This API is especially useful when writing tests.
     * For most production use cases [launchStateProducer] is more suitable.
     */
    public fun <T : State?> setState(key: State.Key<T>, value: T)

    /**
     * Will return the state associated by the given [key] as [StateFlow].
     * Note: Subscribing to the returned [StateFlow] will trigger the 'State Producers' if the given
     * state is cold.
     * See [launchStateProducer]
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
public suspend fun <T : State?> Key<T>.get(): StateFlow<T> {
    return coroutineContext.statesOrThrow.getState(this)
}

public suspend fun <T : State?> Key<T>.getValue(): T {
    return get().value
}

/**
 * Shortcut for `get().collect(collector)`
 * See [get]
 * See [Flow.collect]
 */
public suspend fun <T : State?> Key<T>.collect(collector: FlowCollector<T>) {
    get().collect(collector)
}


/**
 * See [States.setState]
 * @throws [MissingStatesException] if there is no [States] instance installed in the current coroutine context
 */
public suspend fun <T : State?> Key<T>.set(value: T) {
    return coroutineContext.statesOrThrow.setState(this, value)
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
        return getOrCreateMutableStateFlow(key).asStateFlow()
    }

    internal fun <T : State?> getMutableState(key: Key<T>): MutableStateFlow<T> {
        return getOrCreateMutableStateFlow(key)
    }

    private fun <T : State?> getOrCreateMutableStateFlow(key: Key<T>): MutableStateFlow<T> = lock.withLock {
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

