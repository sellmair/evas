package io.sellmair.evas.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.sellmair.evas.State
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @return The current [StateFlow] representing the state associated with the specified key
 * @throws [io.sellmair.evas.MissingStatesException] if no [io.sellmair.evas.States] instance is
 * available in the current composition.
 * See [installStates]
 */
@Composable
public fun <T : State?> State.Key<T>.composeFlow(): StateFlow<T> {
    return statesOrThrow().getState(this)
}

/**
 * Will set the state associated with the specified key to this [value].
 * @throws [io.sellmair.evas.MissingStatesException] if no [io.sellmair.evas.States] instance is
 * available in the current composition.
 * See [installStates]
 */
@Composable
public fun <T : State?> State.Key<T>.composeSet(value: T) {
    statesOrThrow().setState(this, value)
}

/**
 * @return The state associated with the specified key as [androidx.compose.runtime.State]
 * @throws [io.sellmair.evas.MissingStatesException] if no [io.sellmair.evas.States] instance is
 * available in the current composition.
 * See [installStates]
 */
@Composable
public fun <T : State?> State.Key<T>.composeState(): androidx.compose.runtime.State<T> {
    return composeFlow().collectAsState(Dispatchers.Main.immediate)
}

/**
 * @return The state value associated with the specified key. Note: This will bind to 'compose' by subscribing
 * to the value. State changes will cause re-composition (Shortcut for `composeFlow().collectAsState().value`)
 *
 * @throws [io.sellmair.evas.MissingStatesException] if no [io.sellmair.evas.States] instance is
 * available in the current composition.
 * See [installStates]
 */
@Composable
public fun <T : State?> State.Key<T>.composeValue(): T {
    return composeFlow().collectAsState().value
}

/**
 * Convenience wrapper for launching coroutines in callbacks
 * ## Example Usage
 * ### Button onClick
 * ```kotlin
 * Button(
 *     onClick = EvasLaunching {
 *         UserLogoutEvent.emit()
 *     }
 * ) {
 *    Text("Logout")
 * }
 * ```
 *
 * Which is equivalent to
 * ```kotlin
 * val coroutineScope = rememberEvasCoroutineScope()
 * Button(
 *     onClick = {
 *          coroutineScope.launch {
 *             UserLogoutEvent.emit()
 *         }
 *     }
 * ) {
 *    Text("Logout")
 * }
 * ```
 *
 */
@Composable
public inline fun EvasLaunching(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    crossinline effect: suspend () -> Unit
): () -> Unit {
    val scope = rememberEvasCoroutineScope { context }
    return {
        scope.launch(start = start) { effect() }
    }
}

/**
 * Similar to [EvasLaunching], but wrapping a lambda with one parameter
 */
@Composable
public fun <T> EvasLaunching(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    effect: suspend (value: T) -> Unit
): (T) -> Unit {
    val scope = rememberEvasCoroutineScope { context }
    return { value ->
        scope.launch(start = start) { effect(value) }
    }
}