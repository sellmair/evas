@file:Suppress("unused")

package io.sellmair.evas.benchmark

import io.sellmair.evas.*
import io.sellmair.evas.State
import kotlinx.benchmark.*
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture
import kotlin.random.Random

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@kotlinx.benchmark.State(Scope.Benchmark)
open class HotStateProducerBenchmark {

    private lateinit var states: States
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var random: Random

    private var lastReceivedA: StateA? = null
    private var lastReceivedB: StateB? = null

    data class StateA(val id: Any) : State {
        companion object Key : State.Key<StateA?> {
            override val default: StateA? = null
        }
    }

    data class StateB(val id: Any) : State {
        companion object Key : State.Key<StateB?> {
            override val default: StateB? = null
        }
    }

    @Setup
    fun prepare() {
        random = Random(42)
        states = States()
        coroutineScope = CoroutineScope(Dispatchers.Default + Job() + states)

        coroutineScope.launch {
            StateA.collect { value -> lastReceivedA = value }
        }

        coroutineScope.launch {
            StateB.collect { value -> lastReceivedB = value }
        }
    }

    @TearDown
    fun cleanup() {
        coroutineScope.cancel()
        runBlocking {
            coroutineScope.coroutineContext.job.cancelAndJoin()
        }

        check(lastReceivedA == states.getState(StateA).value)
        check(lastReceivedB == states.getState(StateB).value)
    }

    @Benchmark
    fun updateStateUsingSetState() = runBlocking(states) {
        states.setState(StateA, StateA(id = random.nextInt()))
    }

    @Benchmark
    fun updateStateUsingProducer(): Unit = coroutineScope.launchStateProducer(StateA, Dispatchers.Unconfined) {
        StateA(id = random.nextInt()).emit()
    }.job.asCompletableFuture().join()

    @Benchmark
    fun twoStateProducers() = runBlocking(states) {
        coroutineScope {
            coroutineScope.launchStateProducer(StateA, Dispatchers.Unconfined) {
                StateA(id = random.nextInt()).emit()
            }

            coroutineScope.launchStateProducer(StateB, Dispatchers.Unconfined) {
                StateB(id = random.nextInt()).emit()
            }
        }
    }
}