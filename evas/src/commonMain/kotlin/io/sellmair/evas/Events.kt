package io.sellmair.evas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.js.JsName

public interface Event

@JsName("createEvents")
public fun Events(): Events = EventsImpl()

public interface Events : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    public val events: SharedFlow<Event>
    public suspend fun emit(event: Event)

    public companion object Key : CoroutineContext.Key<Events>
}

private class EventsImpl : Events {
    private val eventsImpl = MutableSharedFlow<Event>()
    override val events: SharedFlow<Event> = eventsImpl.asSharedFlow()

    override suspend fun emit(event: Event) {
        eventsImpl.emit(event)
    }
}

public val CoroutineContext.eventsOrNull: Events?
    get() = this[Events]

public val CoroutineContext.eventsOrThrow: Events
    get() = this[Events] ?: throw MissingEventsException(
        "Missing ${Events::class.simpleName} in coroutine context"
    )

public suspend inline fun <reified T : Event> events(): Flow<T> {
    return coroutineContext.eventsOrThrow.events.filterIsInstance<T>().buffer(Channel.UNLIMITED)
}

public suspend inline fun <reified T : Event> collectEvents(noinline collector: suspend (T) -> Unit) {
    events<T>().collect(collector)
}

public inline fun <reified T : Event> CoroutineScope.collectEventsAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    noinline collector: suspend (T) -> Unit
): Job = launch(context = context) { collectEvents<T>(collector) }

public suspend fun Event.emit() {
    coroutineContext.eventsOrThrow.emit(this)
}

@UnstableEvasApi
public val <T> FlowCollector<T>.emit: suspend T.() -> Unit get() = { emit(this) }