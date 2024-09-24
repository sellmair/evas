package io.sellmair.evas.benchmark.utils

import io.sellmair.evas.AtomicSnapshotList
import kotlinx.benchmark.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = BenchmarkTimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@State(Scope.Benchmark)
open class AtomicSnapshotBenchmark {

    private var atomicList = AtomicSnapshotList<Int>()

    @Setup
    fun setup() {
        atomicList = AtomicSnapshotList()
    }

    /**
     * jvmBenchmark summary:
     * Benchmark                               Mode  Cnt   Score   Error  Units
     * AtomicSnapshotBenchmark.writeReadWrite  avgt   20  23.781 ± 0.127  ns/op
     *
     * macosArm64Benchmark summary:
     * Benchmark                       Mode  Cnt    Score   Error  Units
     * CreateFlowBenchmark.appendList  avgt   20  158.378 ± 0.612  ns/op
     */
    @Benchmark
    fun writeReadWrite(): Int {
        atomicList.write { it.add(42) }
        try {
            return atomicList.snapshot().size
        } finally {
            atomicList.write { it.clear() }
        }
    }
}