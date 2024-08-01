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
     * 34884.971 ±(99.9%) 94.687 ops/s [Average]
     *   (min, avg, max) = (34636.933, 34884.971, 35054.948), stdev = 109.042
     *   CI (99.9%): [34790.283, 34979.658] (assumes normal distribution)
     *
     * 1000:
     * 3707.745 ±(99.9%) 8.332 ops/s [Average]
     *   (min, avg, max) = (3692.066, 3707.745, 3727.593), stdev = 9.595
     *   CI (99.9%): [3699.414, 3716.077] (assumes normal distribution)
     */
    @Benchmark
    fun emitEvents() = runBlocking(events) {
        repeat(eventsEmitted) {
            EmittedEvent.emit()
        }
    }

    /**
     * 100:
     * 110180.067 ±(99.9%) 466.968 ops/s [Average]
     *   (min, avg, max) = (109035.328, 110180.067, 111090.603), stdev = 537.761
     *   CI (99.9%): [109713.099, 110647.035] (assumes normal distribution)
     *
     *
     *
     *
     * 1000:
     * 10741.565 ±(99.9%) 35.814 ops/s [Average]
     *   (min, avg, max) = (10667.509, 10741.565, 10807.823), stdev = 41.244
     *   CI (99.9%): [10705.751, 10777.379] (assumes normal distribution)
     */
    @Benchmark
    fun emitAsyncEvents(blackhole: Blackhole) {
        repeat(eventsEmitted) {
            events.emitAsync(EmittedEvent)
        }
    }
}