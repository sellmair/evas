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

@Composable
public fun <T : State?> State.Key<T>.get(): StateFlow<T> {
    return statesOrThrow().getState(this)
}

@Composable
public fun <T : State?> State.Key<T>.set(value: T) {
    statesOrThrow().setState(this, value)
}

@Composable
public fun <T : State?> State.Key<T>.collectAsState(): androidx.compose.runtime.State<T> {
    return get().collectAsState(Dispatchers.Main.immediate)
}

@Composable
public fun <T : State?> State.Key<T>.collectAsValue(): T {
    return get().collectAsState().value
}

@Composable
public inline fun LaunchingEvents(
    context: CoroutineContext = EmptyCoroutineContext, crossinline effect: suspend () -> Unit
): () -> Unit {
    val scope = rememberEvasCoroutineScope { context }
    return {
        scope.launch(start = CoroutineStart.UNDISPATCHED, context = Dispatchers.Main.immediate) { effect() }
    }
}

@Composable
public fun <T> LaunchingEvents(
    context: CoroutineContext = EmptyCoroutineContext,
    effect: suspend (value: T) -> Unit
): (T) -> Unit {
    val scope = rememberEvasCoroutineScope { context }
    return { value ->
        scope.launch(start = CoroutineStart.UNDISPATCHED, context = Dispatchers.Main.immediate) { effect(value) }
    }
}