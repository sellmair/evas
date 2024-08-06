import io.sellmair.evas.State
import io.sellmair.evas.States
import io.sellmair.evas.launchStateProducer
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RunBlockingStatesTest {
    private val states = States()

    data class TestState(val value: Int) : State {
        companion object Key : State.Key<TestState?> {
            override val default: TestState? = null
        }
    }

    @Test
    fun `test with run blocking`() {
        runBlocking(states) {
            launchStateProducer(TestState) {
                TestState(42).emit()
            }
        }

        assertEquals(TestState(42), states.getState(TestState).value)
    }
}