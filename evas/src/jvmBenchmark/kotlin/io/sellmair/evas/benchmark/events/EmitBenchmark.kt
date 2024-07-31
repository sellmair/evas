package io.sellmair.evas.benchmark.events

import io.sellmair.evas.*
import kotlinx.benchmark.*
import kotlinx.benchmark.State
import kotlinx.coroutines.*

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@State(Scope.Benchmark)
open class EmitBenchmark {
    data object EmittedEvent : Event

    private lateinit var events: Events
    private lateinit var coroutineScope: CoroutineScope

    @Param("100", "1000")
    var eventsEmitted: Int = 0

    @Setup
    fun prepare(blackhole: Blackhole) {
        events = Events()
        coroutineScope = CoroutineScope(Dispatchers.Unconfined + Job() + events)

        coroutineScope.collectEventsAsync<EmittedEvent>(context = Dispatchers.Unconfined) { event ->
            blackhole.consume(event)
        }
    }

    @TearDown
    fun teardown() {
        coroutineScope.cancel()
    }


    /**
     * 100:
     * 22041.954 ±(99.9%) 65.424 ops/s [Average]
     *   (min, avg, max) = (21899.308, 22041.954, 22188.540), stdev = 75.342
     *   CI (99.9%): [21976.530, 22107.377] (assumes normal distribution)
     *
     * 1000:
     * 2270.030 ±(99.9%) 8.740 ops/s [Average]
     *   (min, avg, max) = (2250.310, 2270.030, 2293.347), stdev = 10.065
     *   CI (99.9%): [2261.290, 2278.770] (assumes normal distribution)
     */
    @Benchmark
    fun emitEvents() = runBlocking(events) {
        repeat(eventsEmitted) {
            EmittedEvent.emit()
        }
    }

    /**
     * 100:
     * 50801.984 ±(99.9%) 117.427 ops/s [Average]
     *   (min, avg, max) = (50399.497, 50801.984, 50986.841), stdev = 135.230
     *   CI (99.9%): [50684.557, 50919.412] (assumes normal distribution)
     *
     * 1000:
     * 5249.367 ±(99.9%) 7.928 ops/s [Average]
     *   (min, avg, max) = (5228.592, 5249.367, 5262.035), stdev = 9.130
     *   CI (99.9%): [5241.439, 5257.295] (assumes normal distribution)
     */
    @Benchmark
    fun emitAsyncEvents(blackhole: Blackhole) {
        repeat(eventsEmitted) {
            events.emitAsync(EmittedEvent)
        }
    }
}