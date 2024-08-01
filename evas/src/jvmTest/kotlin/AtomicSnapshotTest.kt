import io.sellmair.evas.AtomicSnapshotList
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.test.Test

class AtomicSnapshotTest {
    private val list = AtomicSnapshotList<Int>()

    @Operation
    fun clear() = list.write { it.clear() }

    @Operation
    fun add(element: Int) = list.write { it.add(element) }

    @Operation
    fun remove(element: Int) = list.write { it.remove(element) }

    @Operation
    fun getFirst() = list.snapshot().first()

    @Operation
    fun getLast() = list.snapshot().last()

    @Test
    fun stressTest() = StressOptions().check(this::class)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .hangingDetectionThreshold(12)
        .check(this::class)
}