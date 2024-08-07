@file:Suppress("unused")

package io.sellmair.evas.benchmark.states

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

    /*
    4576308.030 ±(99.9%) 145529.768 ops/s [Average]
    (min, avg, max) = (4431945.239, 4576308.030, 5038085.815), stdev = 167592.298
    CI (99.9%): [4430778.261, 4721837.798] (assumes normal distribution)
     */
    @Benchmark
    fun updateStateUsingSetState() = runBlocking(states) {
        states.setState(StateA, StateA(id = random.nextInt()))
    }

    /*
    533939.420 ±(99.9%) 4023.667 ops/s [Average]
    (min, avg, max) = (519574.494, 533939.420, 540640.677), stdev = 4633.661
    CI (99.9%): [529915.753, 537963.087] (assumes normal distribution)
     */
    @Benchmark
    fun updateStateUsingProducer(): Unit = coroutineScope.launchState(StateA, Dispatchers.Unconfined) {
        StateA(id = random.nextInt()).emit()
    }.job.asCompletableFuture().join()

    /*
    198582.539 ±(99.9%) 6930.709 ops/s [Average]
    (min, avg, max) = (174943.301, 198582.539, 208100.109), stdev = 7981.415
    CI (99.9%): [191651.830, 205513.248] (assumes normal distribution)
     */
    @Benchmark
    fun twoStateProducers() = runBlocking(states) {
        coroutineScope {
            coroutineScope.launchState(StateA, Dispatchers.Unconfined) {
                StateA(id = random.nextInt()).emit()
            }

            coroutineScope.launchState(StateB, Dispatchers.Unconfined) {
                StateB(id = random.nextInt()).emit()
            }
        }
    }
}