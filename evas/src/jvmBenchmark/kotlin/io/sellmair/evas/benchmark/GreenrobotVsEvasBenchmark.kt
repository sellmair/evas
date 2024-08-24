@file:OptIn(KotlinxBenchmarkRuntimeInternalApi::class)

package io.sellmair.evas.benchmark

import io.sellmair.evas.*
import kotlinx.benchmark.*
import kotlinx.benchmark.State
import kotlinx.benchmark.internal.KotlinxBenchmarkRuntimeInternalApi
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.openjdk.jmh.annotations.Threads
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.enums.enumEntries

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 15, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class DifferentLibraryComparisonBenchmark {
    data object EmittedEvent : Event


    private lateinit var events: Events
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var executor: ExecutorService
    private lateinit var greenrobotInternalExecutor: ExecutorService

    /* Greenrobot */
    private lateinit var greenrobotEventBus: EventBus

    private val eventsEmitted: Int = 1000

    @Setup
    fun prepare() {
        val blackhole = CommonBlackhole()
        events = Events()
        coroutineScope = CoroutineScope(Dispatchers.Unconfined + Job() + events)

        coroutineScope.collectEventsAsync<EmittedEvent>(context = Dispatchers.Unconfined) { event ->
            blackhole.consume(event)
        }

        greenrobotInternalExecutor = Executors.newFixedThreadPool(4)
        greenrobotEventBus = EventBus.builder().executorService(greenrobotInternalExecutor).build()
        greenrobotEventBus.register(object {
            @Subscribe(threadMode = ThreadMode.POSTING)
            fun onEvent(event: EmittedEvent) {
                blackhole.consume(event)
            }
        })

        executor = Executors.newFixedThreadPool(4)
    }

    @TearDown
    fun teardown() {
        coroutineScope.cancel()
        executor.shutdownNow()
    }


    @Benchmark
    fun emit_evas() {
        repeat(eventsEmitted) {
            events.emitAsync(EmittedEvent)
        }
    }

    @Benchmark
    fun emit_greenrobot() {
        repeat(eventsEmitted) {
            greenrobotEventBus.post(EmittedEvent)
        }
    }

    @Benchmark
    fun emit_multithreaded_evas() {
        val futures = mutableListOf<Future<out Any>>()
        repeat(8) {
            futures += executor.submit {
                repeat(eventsEmitted) {
                    events.emitAsync(EmittedEvent)
                }
            }
        }
        futures.forEach { it.get() }
    }

    @Benchmark
    fun emit_multithreaded_greenrobot() {
        val futures = mutableListOf<Future<out Any>>()
        repeat(8) {
            futures += executor.submit {
                repeat(eventsEmitted) {
                    greenrobotEventBus.post(EmittedEvent)
                }
            }
        }
        futures.forEach { it.get() }
        greenrobotInternalExecutor.shutdown()
        greenrobotInternalExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)
    }

    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
    @Benchmark
    fun startup_evas(blackhole: Blackhole) {
        val events = Events()
        suspend fun collect() = collectEvents<EmittedEvent> { blackhole.consume(it) }
        repeat(100) {
            ::collect.createCoroutine(Continuation(events) { it.getOrThrow() })
        }
        events.events(EmittedEvent::class)
        events.emitAsync(EmittedEvent)
    }

    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
    @Benchmark
    fun startup_greenrobot(blackhole: Blackhole) {
        val eventBus = EventBus()

        repeat(100) {
            eventBus.register(object {
                @Subscribe(threadMode = ThreadMode.POSTING)
                fun onEvent(event: EmittedEvent) {
                    blackhole.consume(event)
                }
            })
        }
        eventBus.post(EmittedEvent)
    }
}