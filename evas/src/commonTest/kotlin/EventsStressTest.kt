package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stress test - emitAsync`() = runTest(Events(), timeout = 10.minutes) {
        val receivedEvents = mutableListOf<TestEvent>()
        
        collectEventsAsync<TestEvent>(context = StandardTestDispatcher(testScheduler)) {
            receivedEvents += it
        }

        testScheduler.advanceUntilIdle()

        coroutineScope {
            repeat(128) { workerIndex ->
                launch(Dispatchers.Default) {
                    repeat(1024 * 8) { eventId ->
                        TestEvent(workerIndex, eventId).emitAsync()
                    }
                    println("Worker: $workerIndex done")
                }
            }
        }


        launch(Dispatchers.Default) {
            while (receivedEvents.size != 128 * 1024 * 8) {
                println(receivedEvents.size)
                testScheduler.advanceUntilIdle()
                println("Rec: ${receivedEvents.size}")
                yield()
            }
            this@runTest.coroutineContext.job.cancelChildren()
        }
    }
}
