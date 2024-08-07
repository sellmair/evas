import io.sellmair.evas.Event
import io.sellmair.evas.Events
import io.sellmair.evas.collectEvents
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.test.Test

data class TestEvent(val id: Int) : Event

class EventsLincheck {

    private val events = Events()

    @Volatile
    private var lastEvent: TestEvent? = null

    private val allEvents = atomic(emptyList<TestEvent>())

    private fun addEvent(event: TestEvent) {
        allEvents.getAndUpdate { events -> events + event }
    }

    init {
        CoroutineScope(Dispatchers.Unconfined + Events()).launch {
            collectEvents<TestEvent> {
                lastEvent = it
                addEvent(it)
            }
        }
    }

    @Operation
    fun emitAsync(value: Int) = events.emitAsync(TestEvent(value))

    @Operation
    suspend fun emit(value: Int) =
        events.emit(TestEvent(value))

    @Test
    fun stressTest() = StressOptions()
        .check(this::class)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .hangingDetectionThreshold(12)
        .check(this::class)
}