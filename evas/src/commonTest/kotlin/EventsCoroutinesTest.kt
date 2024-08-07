package io.sellmair.evas

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
class EventsCoroutinesTest {

    data class TestState(val id: Any) : State {
        companion object Key : State.Key<TestState?> {
            override val default = null
        }
    }

    @Test
    fun `test - listener is registered inline`() = runTest(Events()) {
        val events = currentCoroutineContext().eventsOrThrow as EventsImpl

        events.typedChannels.snapshot().let { channels ->
            assertTrue(
                channels.isEmpty(),
                "Expected no channels to be registered. Found: $channels"
            )
        }

        collectEventsAsync<Event> { /* Nothing */ }

        events.typedChannels.snapshot().let { channels ->
            assertEquals(
                setOf(Event::class), channels.keys,
                "Expected channel to be available immediately"
            )
        }

        coroutineContext.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - listener in scope producer is registered inline`() = runTest(Events() + States()) {
        val events = currentCoroutineContext().eventsOrThrow as EventsImpl
        launchState(TestState, UnconfinedTestDispatcher(testScheduler)) {
            collectEventsAsync<Event> { }
        }

        events.typedChannels.snapshot().let { channels ->
            assertEquals(
                setOf(Event::class), channels.keys,
                "Expected channel to be available immediately"
            )
        }

        coroutineContext.cancelChildren()
    }
}