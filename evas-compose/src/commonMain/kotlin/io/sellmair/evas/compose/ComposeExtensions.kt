package io.sellmair.evas.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.sellmair.evas.State
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
    return get().collectAsState()
}

@Composable
public fun <T : State?> State.Key<T>.collectAsValue(): T {
    return get().collectAsState().value
}

@Composable
public fun LaunchingEvents(context: CoroutineContext = EmptyCoroutineContext, effect: suspend () -> Unit): () -> Unit {
    val scope = rememberEvasCoroutineScope { context }
    return {
        scope.launch { effect() }
    }
}