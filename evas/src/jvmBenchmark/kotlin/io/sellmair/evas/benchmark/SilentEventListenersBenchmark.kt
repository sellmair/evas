@file:Suppress("unused")

package io.sellmair.evas.benchmark

import io.sellmair.evas.Event
import io.sellmair.evas.Events
import io.sellmair.evas.collectEventsAsync
import io.sellmair.evas.emit
import kotlinx.benchmark.*
import kotlinx.coroutines.*

/*
16.07.24, Mac Studio:
Benchmark                                             (silentListeners)   Mode  Cnt        Score       Error  Units
SilentEventListenersBenchmark.benchmarkEmittingEvent                100  thrpt   20  1497191.416 ± 57396.076  ops/s
SilentEventListenersBenchmark.benchmarkEmittingEvent               1000  thrpt   20  1460679.549 ± 30921.727  ops/s
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@State(Scope.Benchmark)
open class SilentEventListenersBenchmark {

    @Param("100", "1000")
    var silentListeners: Int = 0

    private lateinit var events: Events

    private lateinit var coroutineScope: CoroutineScope

    data object NeverEvent : Event

    data object EmittedEvent : Event

    @Setup
    fun prepare(blackhole: Blackhole) {
        events = Events()
        coroutineScope = CoroutineScope(Dispatchers.Default + Job() + events)

        coroutineScope.collectEventsAsync<EmittedEvent>(start = CoroutineStart.UNDISPATCHED) { event ->
            blackhole.consume(event)
        }

        repeat(silentListeners) {
            coroutineScope.collectEventsAsync<NeverEvent>(start = CoroutineStart.UNDISPATCHED) { event ->
                blackhole.consume(event)
            }
        }
    }

    @TearDown
    fun cleanup() {
        coroutineScope.cancel()
    }

    /*
    16.07.24, Mac Studio:
    Benchmark                                             (silentListeners)   Mode  Cnt        Score       Error  Units
    SilentEventListenersBenchmark.benchmarkEmittingEvent                100  thrpt   20  1497191.416 ± 57396.076  ops/s
    SilentEventListenersBenchmark.benchmarkEmittingEvent               1000  thrpt   20  1460679.549 ± 30921.727  ops/s
    */
    @Benchmark
    fun benchmarkEmittingEvent() = runBlocking(events) {
        EmittedEvent.emit()
    }
}