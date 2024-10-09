@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("NAME_CONTAINS_ILLEGAL_CHARS")

package io.sellmair.evas

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class StatesTest {
    data class HotState(val id: Any?) : State {
        companion object Key : State.Key<HotState?> {
            override val default: HotState? = null
        }
    }

    data class ColdState(val id: Any?) : State {
        data class Key(val id: Any? = null) : State.Key<ColdState?> {
            override val default: ColdState? = null
        }
    }

    @Test
    fun `test - cold producer`() = runTest(States()) {
        val collectedStates = mutableListOf<ColdState?>()

        val producer = launchState { key: ColdState.Key ->
            if (key.id is List<*>) return@launchState
            ColdState(key.id).emit()
        }

        launch {
            ColdState.Key("Hello").flow()
                .onEach { value -> collectedStates.add(value) }
                .collect { value ->
                    if (value != null) {
                        cancel()
                        producer.cancel()
                    }
                }
        }

        testScheduler.advanceUntilIdle()
        assertEquals(listOf(null, "Hello"), collectedStates.map { it?.id })
    }

    @Test
    fun `test - cold producer - state reset`() = runTest(States()) {
        launchState { _: ColdState.Key ->
            var i = 0
            while (isActive) {
                ColdState(i).emit()
                yield()
                i++
            }
        }

        launch {
            ColdState.Key().flow().collect { value ->
                if (value != null) cancel()
            }
        }.join()


        assertNotNull(ColdState.Key().flow().value)

        while (isActive) {
            yield()
            if (ColdState.Key().flow().value == null) {
                coroutineContext.job.cancelChildren()
                break
            }
        }
    }

    @Test
    fun `test - hot producer`() = runTest(States()) {
        launchState(HotState.Key) {
            HotState("Hello").emit()
        }

        testScheduler.advanceUntilIdle()
        assertEquals(HotState("Hello"), HotState.flow().value)
        coroutineContext.job.cancelChildren()
    }

    @Test
    fun `test - hot producer - is shared`() = runTest(States()) {
        var isLaunched = false
        launchState(HotState.Key) {
            HotState("Hello").emit()
            assertFalse(isLaunched)
            isLaunched = true
        }

        testScheduler.advanceUntilIdle()
        assertTrue(isLaunched)
        HotState.flow().take(1).collect()
        HotState.flow().take(1).collect()
        coroutineContext.job.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - keepActive`() = runTest(States()) {
        launchState(keepActive = 3.seconds + 1.milliseconds) { _: ColdState.Key ->
            while (currentCoroutineContext().isActive) {
                ColdState(currentTime).emit()
                delay(1.seconds)
            }
        }

        launch {
            ColdState.Key().flow().takeWhile { it == null }.collect()
        }.join()

        testScheduler.advanceTimeBy(1)

        assertEquals(ColdState(0L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(1000L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(2000L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(3000L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(null, ColdState.Key().flow().value)

        coroutineContext.job.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - keepActive - resubscribe`() = runTest(States()) {
        var launched = false
        launchState(keepActive = 2.seconds + 1.milliseconds) { _: ColdState.Key ->
            assertFalse(launched, "Another producer was already launched!")
            launched = true
            while (currentCoroutineContext().isActive) {
                ColdState(currentTime).emit()
                delay(1.seconds)
            }
        }

        launch {
            ColdState.Key().flow().takeWhile { it == null }.collect()
        }.join()

        testScheduler.advanceTimeBy(1)
        assertEquals(ColdState(0L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(1000L), ColdState.Key().flow().value)

        // Launch coroutine that will receive current state and will wait for one more state
        // Giving us three more emissions
        launch {
            ColdState.Key().flow().take(2).collect()
        }

        testScheduler.advanceTimeBy(3.seconds)
        assertEquals(ColdState(4000L), ColdState.Key().flow().value)

        testScheduler.advanceTimeBy(3.seconds)
        assertEquals(null, ColdState.Key().flow().value)

        coroutineContext.job.cancelChildren()
    }

    @Test
    fun `test - hot state producer job - is completed after corotuine finished`() = runTest(States()) {
        val stateProducerJob = launchState(HotState, StandardTestDispatcher(testScheduler)) {
            HotState(42).emit()
        }

        // Wait for the state
        HotState.flow().first { it == HotState(42) }
        assertTrue(stateProducerJob.isCompleted, "Expected job to be completed")
    }

    @Test
    fun `test - hot state producer is started eagerly`() = runTest(States()) {
        launchState(HotState, StandardTestDispatcher(testScheduler)) {
            HotState(42).emit()
        }

        testScheduler.advanceUntilIdle()
        assertEquals(HotState(42), HotState.flow().value)
    }

    @Test
    fun `test - hot state started lazily`() = runTest(States()) {
        var isSubscribed = false

        val stateProducerJob = launchState(HotState, StandardTestDispatcher(testScheduler), StateProducerStarted.Lazily) {
                assertTrue(isSubscribed, "Expected state producer to only be launched after at least one subscription")
                HotState(42).emit()
            }
        testScheduler.advanceUntilIdle()
        assertEquals(
            null, HotState.flow().value,
            "We do not expect any value being emitted, as the state producer was not yet launched"
        )

        // Start subscribing to the state
        isSubscribed = true
        assertEquals(HotState(42), HotState.flow().filterNotNull().first())
        assertTrue(stateProducerJob.isCompleted, "Expected job to be completed")
    }


    @Test
    fun `test - update`() = runTest(States()) {
        assertEquals(States.Update(null, HotState(1)), HotState.update { HotState(1) })

        assertEquals(
            States.Update(HotState(1), HotState(2)),
            HotState.update { state -> state?.copy(id = (state.id as Int).inc()) })

        assertEquals(
            States.Update(HotState(2), null),
            HotState.update { null }
        )
    }

    @Test
    fun `test - getAndUpdate`() = runTest(States()) {
        assertEquals(null, HotState.getAndUpdate { HotState(1) })
        assertEquals(HotState(1), HotState.getAndUpdate { HotState(it!!.id as Int + 1) })
        assertEquals(HotState(2), HotState.getAndUpdate { null })
        assertNull(HotState.getAndUpdate { null })
    }

    @Test
    fun `test - updateAndGet`() = runTest(States()) {
        assertEquals(HotState(1), HotState.updateAndGet { HotState(1) })
        assertEquals(HotState(2), HotState.updateAndGet { it!!.copy(id = (it.id as Int).inc()) })
    }
}