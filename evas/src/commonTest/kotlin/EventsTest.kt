package io.sellmair.evas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventsTest {

    data class TestEvent(val id: Any) : Event
    data class SecondTestEvent(val id: Any) : Event

    @Test
    fun `test - simple event emission - single collector`() = runTest(Events()) {
        val receivedEvents = mutableListOf<TestEvent>()
        collectEventsAsync<TestEvent>(Dispatchers.Unconfined) { receivedEvents += it }

        TestEvent(1).emit()
        coroutineContext.eventsOrThrow.emit(TestEvent(2))

        coroutineContext.cancelChildren()


        assertEquals(listOf(TestEvent(1), TestEvent(2)), receivedEvents)
    }

    @Test
    fun `test - multiple events - multiple collectors`() = runTest(Events()) {
        /* Collector A */
        val receivedEventsA = mutableListOf<TestEvent>()
        collectEventsAsync<TestEvent>(Dispatchers.Unconfined) { receivedEventsA += it }

        /* Collector B */
        val receivedEventsB = mutableListOf<TestEvent>()
        collectEventsAsync<TestEvent>(Dispatchers.Unconfined) { receivedEventsB += it }

        /* Collector C */
        val receivedEventsC = mutableListOf<SecondTestEvent>()
        collectEventsAsync<SecondTestEvent>(Dispatchers.Unconfined) { receivedEventsC += it }

        /* Send events */
        TestEvent(1).emit()
        SecondTestEvent(2).emit()
        TestEvent(3).emit()
        SecondTestEvent(4).emit()

        coroutineContext.cancelChildren()

        assertEquals(listOf(TestEvent(1), TestEvent(3)), receivedEventsA)
        assertEquals(listOf(TestEvent(1), TestEvent(3)), receivedEventsB)
        assertEquals(listOf(SecondTestEvent(2), SecondTestEvent(4)), receivedEventsC)
    }
}