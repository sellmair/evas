import io.sellmair.evas.State
import io.sellmair.evas.States
import io.sellmair.evas.flow
import io.sellmair.evas.launchStateProducer
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.runTest
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
class StatesCoroutinesTest {

    private lateinit var singleThreadDispatcher: ExecutorCoroutineDispatcher
    private val singleThread = atomic<Thread?>(null)

    @BeforeTest
    fun setup() {
        singleThreadDispatcher = Executors.newFixedThreadPool(1, { runnable ->
            Thread(runnable, "Single Thread Executor").also {
                assertNull(singleThread.getAndSet(it))
            }
        }).asCoroutineDispatcher()
    }

    @AfterTest
    fun cleanup() {
        singleThreadDispatcher.close()
    }


    data class TestState(val id: Any) : State {
        companion object Key : State.Key<TestState?> {
            override val default: TestState? = null
        }
    }

    @Test
    fun `test - dispatcher`() = runTest(States()) {
        launchStateProducer(TestState.Key, coroutineContext = singleThreadDispatcher) {
            assert(Thread.currentThread() === singleThread.value)
            TestState(42).emit()
        }

        TestState.flow().first { it == TestState(42) }
    }
}