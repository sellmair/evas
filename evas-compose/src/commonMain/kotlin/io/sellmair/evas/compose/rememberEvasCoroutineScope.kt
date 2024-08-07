package io.sellmair.evas.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Similar to [rememberCoroutineScope], but will automatically bring [io.sellmair.evas.Events] and
 * [io.sellmair.evas.States] into the coroutine context (if available)
 * @see rememberCoroutineScope
 */
@Composable
public inline fun rememberEvasCoroutineScope(
    crossinline getContext: @DisallowComposableCalls () -> CoroutineContext = { EmptyCoroutineContext }
): CoroutineScope {
    val events = eventsOrNull() ?: EmptyCoroutineContext
    val states = statesOrNull() ?: EmptyCoroutineContext
    return rememberCoroutineScope {
        events + states + getContext()
    }
}