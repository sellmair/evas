package io.sellmair.evas

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsName
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Marker interface for all events which can be dispatched with evas.
 *
 * ### Example Usages
 * #### An event representing the user logging out of the application:
 * ```kotlin
 * data object UserLoggedOutEvent: Event
 *               //                  ^
 *               // Marked as 'Event', can be dispatched
 *
 * @Composable
 * fun LogoutButton() {
 *     Button(
 *         onClick = LaunchingEvents {
 *             UserLoggedOutEvent.emit()
 *         }
 *     ) {
 *         // ...
 *     }
 * }
 * ```
 */
public interface Event

/**
 * Factory Function: Creates a new instance of [Events]
 * @see Events
 */
@JsName("createEvents")
public fun Events(): Events = EventsImpl()

/**
 * The 'Event Bus' which allows sending events to (using the [emit] function), but also
 * receiving all events, by collecting the [events] shared flow.
 *
 * ## Example Usages
 *
 * ### Create a new instance
 * A new instance can be created by the [Events] function:
 * ```kotlin
 * val events = Events()
 * ```
 *
 * ### Installing in the current coroutine context:
 * Whilst it is possible to pass the [Events] instance around, using any kind of dependency injection
 * mechanism, an easier and more pragmatic way is installing the event bus in the current coroutine context.
 *
 * ```kotlin
 * suspend fun main() {
 *     withContext(Events()) {
 *         // ...
 *     }
 * }
 * ```
 *
 * ### Emitting events from coroutines with installed [Events]
 * With the [Events] container being installed, emitting an event can be done using the [Event.emit] extension:
 * ```kotlin
 * suspend fun logout() {
 *     UserLoggedOutEvent.emit()
 *     //                   ^
 *     //     Will dispatch the UserLoggedOutEvent
 *     //     to the 'Events' instance which is in the current context.
 * }
 * ```
 *
 * ### Installing in the current composition (using `io.sellmair:evas-compose`)
 * ```kotlin
 * @Composable
 * fun App() {
 *      val events = Events()
 *      // ...
 *      installEvents(events) { // <- Will register the 'events' instance as Composition Local
 *          MainPage() // <- another 'Composable' function
 *      }
 * }
 * ```
 * See: https://developer.android.com/reference/kotlin/androidx/compose/runtime/CompositionLocal
 *
 * ### Emitting events from a composable function with installed [Events]
 * Since the [Events.emit] function is suspending, the emission of the event requires launching
 * a coroutine. The launched coroutine is requried to ahve the [Events] install in context:
 *
 * ```kotlin
 * @Composable
 * fun MyButton() {
 *
 *     // Option 1
 *     val coroutineScope = rememberCoroutineScope { eventsOrThrow }
 *
 *     // Option 2
 *     val coroutineScope = rememberEvasCoroutineScope()
 *     //                             ^
 *     //             Brings 'Events' into the coroutine scope
 *
 *     Button(
 *         onClick = {
 *             coroutineScope.launch {
 *                 MyEvent().emit()
 *             }
 *         }
 *     ) {
 *         // ...
 *     }
 * }
 * ```
 *
 * There is also a simpler function called `LaunchingEvent` which can be used:
 * ```kotlin
 * @Composable
 * fun MyButton() {
 *     Button(
 *         onClick = LaunchingEvent {
 *              MyEvent().emit()
 *         }
 *     ){
 *         // ...
 *     }
 * }
 * ```
 * This method will wrap the coroutine launching for you.
 *
 * ### Collecting events
 * Collecting events is most convenient using the [collectEvents] or [collectEventsAsync] methods.
 * ```kotlin
 * suspend fun deleteCachesOnUserLoggedOut(cache: Cache) {
 *     collectEvents<UserLoggedOutEvent>() { event ->
 *         cache.deleteDataFrom(event.user)
 *     }
 * }
 * ```
 *
 */
public sealed interface Events : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    public suspend fun emit(event: Event)

    public fun <T : Event> events(clazz: KClass<T>): Flow<T>

    public companion object Key : CoroutineContext.Key<Events>
}

private class EventsImpl : Events {
    private val eventsImpl = MutableSharedFlow<Event>()
    private val events: SharedFlow<Event> = eventsImpl.asSharedFlow()

    private val typedEventFlows = mutableMapOf<KClass<*>, SharedFlow<*>>()
    private val typedEventFlowsLock = reentrantLock()

    override fun <T : Event> events(clazz: KClass<T>): Flow<T> = typedEventFlowsLock.withLock {
        @Suppress("UNCHECKED_CAST")
        typedEventFlows.getOrPut(clazz) {
            events.mapNotNull { event -> clazz.safeCast(event) }
                .buffer(Channel.UNLIMITED)
                .shareIn(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined), SharingStarted.WhileSubscribed())
        } as SharedFlow<T>
    }

    override suspend fun emit(event: Event) {
        eventsImpl.emit(event)
    }
}

/**
 * @return the currently installed [Events] or null.
 * See [Events] on how to install an instance in the current coroutine context.
 */
public val CoroutineContext.eventsOrNull: Events?
    get() = this[Events]

/**
 * @return the currently installed [Events] or throws a [MissingEventsException]
 * See [Events] on how to install an instance in the current coroutine context.
 */
public val CoroutineContext.eventsOrThrow: Events
    get() = this[Events] ?: throw MissingEventsException(
        "Missing ${Events::class.simpleName} in coroutine context"
    )

/**
 * Creates a new buffered flow for all events of type [T].
 * The buffer is 'unlimited', events will be processed in order.
 */
public suspend inline fun <reified T : Event> events(): Flow<T> {
    return coroutineContext.eventsOrThrow.events(T::class)
}

/**
 * Will suspend and collect all events of a certain type:
 * Requires [Events] to be installed to the current coroutine context.
 *
 * ## Example Usages
 * ### Waiting for a user to be logged out to delete caches
 * ```kotlin
 * suspend fun deleteCachesOnUserLoggedOut(cache: Cache) {
 *     collectEvents<UserLoggedOut>() { event ->
 *         cache.deleteForUser(event.user)
 *     }
 * }
 * ```
 */
public suspend inline fun <reified T : Event> collectEvents(noinline collector: suspend (T) -> Unit) {
    events<T>().collect(collector)
}

/**
 * Launches a new coroutine (see [launch]) which starts collecting all events of type [T].
 * This uses [events] under the hood, which will ensure all events being buffered, which will prevent
 * event emitters from suspending.
 *
 * Shortcut for
 * ```kotlin
 * launch(context = context, start = start) { collectEvents<T>(collector) }
 * ```
 *
 * ## Example Usages
 * ### Waiting for a user to be logged out to delete caches
 * ```kotlin
 * fun CoroutineScope.deleteCachesOnUserLoggedOut(cache: Cache): Job = collectEventsAsync<UserLoggedOut>() { event ->
 *         cache.deleteForUser(event.user)
 * }
 * ```
 */
public inline fun <reified T : Event> CoroutineScope.collectEventsAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline collector: suspend (T) -> Unit
): Job = launch(context = context, start = start) { collectEvents<T>(collector) }

/**
 * Sends the current [Event] by dispatching it to the [Events] instance currently available
 * in the current coroutine context.
 *
 * @throws MissingEventsException if there is no [Events] instance installed in the current coroutine context.
 * See [Events] documentation to learn more.
 */
public suspend fun Event.emit() {
    coroutineContext.eventsOrThrow.emit(this)
}

@UnstableEvasApi
public val <T> FlowCollector<T>.emit: suspend T.() -> Unit get() = { emit(this) }