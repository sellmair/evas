package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsName
import kotlin.reflect.KClass

/**
 * Marker interface for all events which can be dispatched with evas.
 *
 *
 * ### Example Usages (Compose)
 * #### An event representing the user logging out of the application:
 * ```kotlin
 * data object UserLoggedOutEvent: Event
 *               //                  ^
 *               // Marked as 'Event', can be dispatched
 * ```
 *
 * ### Emitting this event from coroutine context
 * ```
 * suspend fun logout() {
 *     deleteUserData()
 *     UserLoggedOutEvent.emit()
 *                   //    ^
 *                   // Will dispatch the Events bus installed in the current coroutine context
 * }
 * ```
 *
 * #### Emitting this event in compose
 * ```
 * @Composable
 * fun LogoutButton() {
 *     Button(
 *         onClick = EvasLaunching {
 *             UserLoggedOutEvent.emit()
 *         }
 *     ) {
 *         // ...
 *     }
 * }
 *
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
 * There is also a simpler function called [io.sellmair.evas.compose.EvasLaunching] which can be used:
 * ```kotlin
 * @Composable
 * fun MyButton() {
 *     Button(
 *         onClick = EvasLaunching {
 *              MyEvent().emit()
 *         }
 *     ){
 *         // ...
 *     }
 * }
 * ```
 * This method will wrap the coroutine launching for you.
 *
 * ### Collecting events * is most convenient using the [collectEvents] or [collectEventsAsync] methods.
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

    /**
     * See: [Event.emit]
     */
    public suspend fun emit(event: Event)

    /**
     * See: [Event.emitAsync]
     */
    public fun emitAsync(event: Event)

    /**
     * @return [Flow] containing all events implementing [clazz]
     * Note: All events that are subclasses of the specified [clazz] will also be contained
     */
    public fun <T : Event> events(clazz: KClass<T>): Flow<T>

    public companion object Key : CoroutineContext.Key<Events>
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
    start: CoroutineStart = CoroutineStart.UNDISPATCHED,
    noinline collector: suspend (T) -> Unit
): Job = launch(context = context, start = start) {
    collectEvents<T>(collector)
}

/**
 * Sends the current [Event] by dispatching it to the [Events] instance currently installed
 * in the current coroutine context.
 *
 * ☝️ Note: This method will suspend until all listener coroutines have finished processing this event:
 * e.g.
 * ```
 * suspend fun example() = coroutineScope {
 *    collectEventsAsync<MyEvent> {
 *        delay(5.seconds)
 *        print("First collector finished")
 *
 *    }
 *
 *    collectEventsAsync<MyEvent> {
 *        delay(1.seconds)
 *        print("Second collector finished")
 *    }
 *
 *    MyEvent().emit()
 *             // ^
 *             // Will suspend ~ 5 seconds as the first
 *             // collector seems to be the slowest, taking ~ 5 seconds
 *
 *    println(".emit() finished")
 *
 *    // Will print
 *    // > Second collector finished
 *    // > First collector finished
 *    // > .emit() finished
 * }
 * ```
 *
 * @throws MissingEventsException if there is no [Events] instance installed in the current coroutine context.
 * See [Events] documentation to learn more.
 * See [emitAsync] for emitting an event without waiting for all listeners to finish their work.
 *
 */
public suspend fun Event.emit() {
    coroutineContext.eventsOrThrow.emit(this)
}

/**
 * Similar to [Event.emit] with one distinction:
 * While [Event.emit] will suspend until all event listener coroutines have finished,
 * this [Event.emitAsync] implementation will not suspend.
 *
 * Events can be emitted async even outside suspend functions, by calling
 * [Events.emitAsync] directly
 *
 * @throws MissingEventsException if there is no [Events] instance installed in the current coroutine context.
 */
public suspend fun Event.emitAsync() {
    coroutineContext.eventsOrThrow.emitAsync(this)
}

@UnstableEvasApi
public val <T> FlowCollector<T>.emit: suspend T.() -> Unit get() = { emit(this) }

/*
Implementation!
*/

internal class EventsImpl : Events {
    private val unconfinedScope = CoroutineScope(Dispatchers.Unconfined)

    internal val typedChannels = AtomicSnapshotMap<KClass<*>, AtomicSnapshot<MutableList<Channel<Dispatch<*>>>, List<Channel<Dispatch<*>>>>>()

    internal class Dispatch<out T>(val event: T, val job: CompletableJob?)

    override fun <T : Event> events(clazz: KClass<T>): Flow<T> {
        val dispatchFlow = flow {
            val channel = Channel<Dispatch<T>>(Channel.UNLIMITED)

            /* Register Channel in this even bus */
            typedChannels.write { mutableTypedChannels ->
                val channels = mutableTypedChannels.getOrPut(clazz) { AtomicSnapshotList() }

                channels.write { mutableChannels ->
                    @Suppress("UNCHECKED_CAST")
                    mutableChannels.add(channel as Channel<Dispatch<*>>)
                }
            }

            currentCoroutineContext().job.invokeOnCompletion {
                /* Cleanup: We can remove the channel from the event bus */
                typedChannels.write { mutableTypedChannels ->
                    @Suppress("UNCHECKED_CAST")
                    channel as Channel<Dispatch<*>>
                    val channels = mutableTypedChannels[clazz]
                    channels?.write { mutableChannels ->
                        mutableChannels.remove(channel)
                        if (mutableChannels.isEmpty()) mutableTypedChannels.remove(clazz)
                    }
                }
            }

            emitAll(channel)
        }

        return flow {
            dispatchFlow.collect { dispatch ->
                try {
                    emit(dispatch.event)
                } finally {
                    dispatch.job?.complete()
                }
            }
        }
    }

    override suspend fun emit(event: Event) {
        coroutineScope {
            createChannelQueue(event).forEach { channel ->
                /* Launching coroutine and waiting for the listener to finish (passing non-null job) */
                launch(context = Dispatchers.Unconfined, start = CoroutineStart.UNDISPATCHED) {
                    val job = Job()
                    val dispatch = Dispatch(event, job)
                    try {
                        channel.send(dispatch)
                        job.join()
                    } catch (e: ClosedSendChannelException) {
                        return@launch
                    }
                }
            }
        }
    }

    override fun emitAsync(event: Event) {
        val queue = createChannelQueue(event)
        queue.forEach { listener ->
            val dispatch = Dispatch(event, null)
            val sendResult = listener.trySend(dispatch)
            if (sendResult.isClosed) return@forEach
            if (sendResult.isFailure) {
                unconfinedScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    listener.send(dispatch)
                }
            }
        }
    }

    private fun createChannelQueue(event: Event): List<SendChannel<Dispatch<Event>>> {
        val listenerQueue = mutableListOf<SendChannel<Dispatch<Event>>>()
        typedChannels.snapshot().forEach { (clazz, listeners) ->
            if (clazz.isInstance(event)) {
                listenerQueue.addAll(listeners.snapshot())
            }
        }

        return listenerQueue
    }
}
