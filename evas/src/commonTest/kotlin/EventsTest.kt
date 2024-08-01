package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
class EventsTest {

    data class TestEventA(val id: Any) : Event
    data class TestEventB(val id: Any) : Event

    @Test
    fun `test - simple event emission - single collector`() = runTest(Events()) {
        val receivedEvents = mutableListOf<TestEventA>()
        collectEventsAsync<TestEventA>(Dispatchers.Unconfined) { receivedEvents += it }

        TestEventA(1).emit()
        coroutineContext.eventsOrThrow.emit(TestEventA(2))

        coroutineContext.cancelChildren()


        assertEquals(listOf(TestEventA(1), TestEventA(2)), receivedEvents)
    }

    @Test
    fun `test - multiple events - multiple collectors`() = runTest(Events()) {
        /* Collector A */
        val receivedEventsA = mutableListOf<TestEventA>()
        collectEventsAsync<TestEventA>(Dispatchers.Unconfined) { receivedEventsA += it }

        /* Collector B */
        val receivedEventsB = mutableListOf<TestEventA>()
        collectEventsAsync<TestEventA>(Dispatchers.Unconfined) { receivedEventsB += it }

        /* Collector C */
        val receivedEventsC = mutableListOf<TestEventB>()
        collectEventsAsync<TestEventB>(Dispatchers.Unconfined) { receivedEventsC += it }

        /* Send events */
        TestEventA(1).emit()
        TestEventB(2).emit()
        TestEventA(3).emit()
        TestEventB(4).emit()

        coroutineContext.cancelChildren()

        assertEquals(listOf(TestEventA(1), TestEventA(3)), receivedEventsA)
        assertEquals(listOf(TestEventA(1), TestEventA(3)), receivedEventsB)
        assertEquals(listOf(TestEventB(2), TestEventB(4)), receivedEventsC)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - slow subscriber does not block other emissions`() = runTest(Events()) {
        val receivedEventsA = mutableListOf<TestEventA>()
        val receivedEventsB = mutableListOf<TestEventB>()

        // Slow subscriber
        collectEventsAsync<TestEventB>(context = UnconfinedTestDispatcher(testScheduler)) {
            delay(1000)
            receivedEventsB += it
        }


        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            receivedEventsA += it
        }

        testScheduler.advanceUntilIdle()
        TestEventA(1).emit()
        testScheduler.advanceUntilIdle()
        assertEquals(listOf(TestEventA(1)), receivedEventsA)
        assertEquals(0, testScheduler.currentTime)

        TestEventA(2).emit()
        assertEquals(listOf(TestEventA(1), TestEventA(2)), receivedEventsA)
        assertEquals(0, testScheduler.currentTime)

        TestEventB(3).emit()
        assertEquals(listOf(TestEventB(3)), receivedEventsB)
        assertEquals(1000, testScheduler.currentTime)

        coroutineContext.job.cancelChildren()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test - slow and fast subscriber loops`() = runTest(Events()) {
        val receivedEventsA = mutableListOf<TestEventA>()
        val receivedEventsB = mutableListOf<TestEventB>()

        // fast
        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            delay(1)
            receivedEventsA += it
        }

        // slow
        collectEventsAsync<TestEventB>(context = UnconfinedTestDispatcher(testScheduler)) {
            delay(10)
            receivedEventsB += it
        }

        // Fast emissions
        val fast = launch(StandardTestDispatcher(testScheduler)) {
            repeat(1_000) {
                TestEventA(it).emit()
            }
        }

        // Slow emissions
        val slow = launch(StandardTestDispatcher(testScheduler)) {
            repeat(1_000) {
                TestEventB(it).emit()
            }
        }

        fast.join()
        assertEquals(buildList { repeat(1_000) { add(TestEventA(it)) } }, receivedEventsA)
        assertEquals(TestEventB(99), receivedEventsB.last())

        slow.join()
        assertEquals(TestEventB(999), receivedEventsB.last())
        coroutineContext.job.cancelChildren()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test - event emission waits for all subscribers to finish`() = runTest(Events()) {
        val receivedSubscriber1 = mutableListOf<TestEventA>()
        val receivedSubscriber2 = mutableListOf<TestEventA>()

        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            receivedSubscriber1 += it
            delay(1)
        }

        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            receivedSubscriber2 += it
            delay(10)
        }

        TestEventA(1).emit()
        assertEquals(listOf(TestEventA(1)), receivedSubscriber1)
        assertEquals(listOf(TestEventA(1)), receivedSubscriber2)

        TestEventA(2).emit()
        assertEquals(listOf(TestEventA(1), TestEventA(2)), receivedSubscriber1)
        assertEquals(listOf(TestEventA(1), TestEventA(2)), receivedSubscriber2)

        currentCoroutineContext().job.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - event listeners are executed in parallel`() = runTest(Events()) {
        data class Checkpoint(val name: String)

        val collectorAWaiting = Checkpoint("CollectorAWaiting")
        val collectorBWaiting = Checkpoint("CollectorBWaiting")
        val collectorAFinished = Checkpoint("CollectorAFinished")
        val collectorBFinished = Checkpoint("CollectorBFinished")

        var checkpoint: Checkpoint? = null

        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(null, checkpoint)
            checkpoint = collectorAWaiting
            delay(10)
            assertEquals(10, testScheduler.currentTime)
            checkpoint = collectorAFinished
        }

        collectEventsAsync<TestEventA>(context = UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(collectorAWaiting, checkpoint)
            checkpoint = collectorBWaiting
            delay(20)
            assertEquals(20, testScheduler.currentTime)

            assertEquals(collectorAFinished, checkpoint)
            checkpoint = collectorBFinished
        }

        TestEventA(0).emit()
        assertEquals(collectorBFinished, checkpoint)
        currentCoroutineContext().job.cancelChildren()
    }

    @Test
    fun `test - exception`() = runTest(Events()) {
        val exceptions = mutableListOf<Throwable>()
        val exception = IllegalStateException("Test Exception")
        val handler = CoroutineExceptionHandler { _, throwable ->
            exceptions += throwable
        }

        collectEventsAsync<TestEventA>(context = NonCancellable + StandardTestDispatcher(testScheduler) + handler) { event ->
            if (event == TestEventA(10)) {
                throw exception
            }
        }

        /* Advance to ensure that the collector is active */
        testScheduler.advanceUntilIdle()
        TestEventA(1).emit()
        TestEventA(10).emit()

        testScheduler.advanceUntilIdle()
        assertSame(exception.message, exceptions.single().message)
    }
}