package io.sellmair.evas

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
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

        val producer = launchStateProducer { key: ColdState.Key ->
            if (key.id is List<*>) return@launchStateProducer
            ColdState(key.id).emit()
        }

        launch {
            ColdState.Key("Hello").get()
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
        launchStateProducer { _: ColdState.Key ->
            var i = 0
            while (isActive) {
                ColdState(i).emit()
                yield()
                i++
            }
        }

        launch {
            ColdState.Key().get().collect { value ->
                if (value != null) cancel()
            }
        }.join()


        assertNotNull(ColdState.Key().get().value)

        while (isActive) {
            yield()
            if (ColdState.Key().get().value == null) {
                coroutineContext.job.cancelChildren()
                break
            }
        }
    }

    @Test
    fun `test - hot producer`() = runTest(States()) {
        launchStateProducer(HotState.Key) {
            HotState("Hello").emit()
        }

        testScheduler.advanceUntilIdle()
        assertEquals(HotState("Hello"), HotState.get().value)
        coroutineContext.job.cancelChildren()
    }

    @Test
    fun `test - hot producer - is shared`() = runTest(States()) {
        var isLaunched = false
        launchStateProducer(HotState.Key) {
            HotState("Hello").emit()
            assertFalse(isLaunched)
            isLaunched = true
        }

        testScheduler.advanceUntilIdle()
        assertTrue(isLaunched)
        HotState.get().take(1).collect()
        HotState.get().take(1).collect()
        coroutineContext.job.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - keepActive`() = runTest(States()) {
        launchStateProducer(keepActive = 3.seconds + 1.milliseconds) { _: ColdState.Key ->
            while(currentCoroutineContext().isActive) {
                ColdState(currentTime).emit()
                delay(1.seconds)
            }
        }

        launch {
            ColdState.Key().get().takeWhile { it == null }.collect()
        }.join()

        testScheduler.advanceTimeBy(1)

        assertEquals(ColdState(0L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(1000L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(2000L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(3000L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(null, ColdState.Key().get().value)

        coroutineContext.job.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test - keepActive - resubscribe`() = runTest(States()) {
        var launched = false
        launchStateProducer(keepActive = 2.seconds + 1.milliseconds) { _: ColdState.Key ->
            assertFalse(launched, "Another producer was already launched!")
            launched = true
            while(currentCoroutineContext().isActive) {
                ColdState(currentTime).emit()
                delay(1.seconds)
            }
        }

        launch {
            ColdState.Key().get().takeWhile { it == null }.collect()
        }.join()

        testScheduler.advanceTimeBy(1)
        assertEquals(ColdState(0L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(ColdState(1000L), ColdState.Key().get().value)

        // Launch coroutine that will receive current state and will wait for one more state
        // Giving us three more emissions
        launch {
            ColdState.Key().get().take(2).collect()
        }

        testScheduler.advanceTimeBy(3.seconds)
        assertEquals(ColdState(4000L), ColdState.Key().get().value)

        testScheduler.advanceTimeBy(3.seconds)
        assertEquals(null, ColdState.Key().get().value)

        coroutineContext.job.cancelChildren()
    }
}