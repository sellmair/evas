package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventsStressTest {

    data class TestEvent(val coroutineId: Int, val eventId: Int) : Event

    @Test
    fun `test - many events`() = runTest(Events()) {
        val receivedEvents = mutableListOf<TestEvent>()

        /* Collector started 'UNDISPATCHED' to not miss early emissions of coroutines started below. */
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            collectEvents<TestEvent> { receivedEvents += it }
        }

        /* Launch many event coroutines emitting events */
        val coroutines = 128
        val emissionsPerCoroutine = 1024 * 8
        coroutineScope {
            withContext(Dispatchers.Default) {
                repeat(coroutines) { coroutineId ->
                    launch {
                        repeat(emissionsPerCoroutine) { eventId ->
                            TestEvent(coroutineId, eventId).emit()
                        }
                    }
                }
            }
        }

        coroutineContext.cancelChildren()
        collector.join()

        receivedEvents.groupBy { event -> event.coroutineId }.forEach { (coroutineId, events) ->
            val expected = buildList {
                repeat(emissionsPerCoroutine) { eventId ->
                    add(TestEvent(coroutineId, eventId))
                }
            }

            assertEquals(expected, events)
        }
    }
}
