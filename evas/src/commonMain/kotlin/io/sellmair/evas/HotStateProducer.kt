package io.sellmair.evas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Launches a new coroutine, which can [io.sellmair.evas.emit] states associated with the provided [key].
 *
 * ## Example Usages
 * ### State class with a companion object key
 * ```kotlin
 * data class EmailState(val email: String, val isValid: Boolean): State {
 *     companion object Key : State.Key<EmailState?> {
 *                                             // ^
 *                                             // State can be absent (nullable)
 *
 *         val default: EmailState? = null
 *     }
 * }
 * ```
 *
 * Launching a state typically will look like
 * ```kotlin
 * fun CoroutineScope.launchEmailState() = launchState(EmailState) {
 *     collectEvents<EmailChangedEvent> { event ->
 *         EmailState(event.email, isValid = validateEmail(event.email)).emit()
 *     }
 * }
 * ```
 * @param context Additional context used for [produce]
 *
 * @param started Allows controlling when the 'state producing coroutine' is launched
 * - default: [StateProducerStarted.Eagerly]: The coroutine will launch as soon as possible
 * - option: [StateProducerStarted.Lazily]: The coroutine will be launched when at least one other
 * coroutine subscribed to it
 *
 */
public fun <T : State?> CoroutineScope.launchState(
    key: State.Key<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    started: StateProducerStarted = StateProducerStarted.Eagerly,
    produce: suspend StateProducerScope<T>.() -> Unit
): Job {
    val newCoroutineContext = (this.coroutineContext + context)
    val states = newCoroutineContext.statesOrThrow
    val coroutineScope = CoroutineScope(newCoroutineContext)

    return when (started) {
        StateProducerStartedEagerly -> coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            states.setState(key, stateProducerFlow(produce))
        }

        StateProducerStartedLazily -> coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            /* We first await at least one subscriber */
            states.internal.getMutableState(key).subscriptionCount.first { subscriptionsCount ->
                subscriptionsCount > 0
            }

            /* Then we can start emitting the state */
            states.setState(key, stateProducerFlow(produce))
        }
    }
}
