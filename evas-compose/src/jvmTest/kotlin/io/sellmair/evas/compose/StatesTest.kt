package io.sellmair.evas.compose

import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import io.sellmair.evas.State
import io.sellmair.evas.States
import io.sellmair.evas.launchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class StatesTest {

    /**
     * This is a requirement for compose:
     * Let's assume we're only consuming a cold flow in a single @Composable function
     *
     * ```kotlin
     * @Composable
     * fun MyWidget() {
     *     Text(MyState.composeValue())
     * }
     * ```
     *
     * Then this 'composeValue' function will internally get the 'StateFlow' and use the 'collectAsState' compose
     * function to react to the state:
     *
     * ```kotlin
     * /* Copied form compose */
     * @Composable
     * fun <T> produceState(
     *     initialValue: T,
     *     key1: Any?, // < this key will be the StateFlow which shall be collected
     *     key2: Any?, // <- this key will be the current coroutine context
     *     producer: suspend ProduceStateScope<T>.() -> Unit
     * ): State<T> {
     *     val result = remember { mutableStateOf(initialValue) }
     *     LaunchedEffect(key1, key2) {
     *         ProduceStateScopeImpl(result, coroutineContext).producer()
     *     }
     *     return result
     * }
     *
     * ```
     *
     * As you can see: It is important to return the same (or equal) instance of this 'to be collected' flow
     * as otherwise the LaunchedEffect will be re-triggered and "subscribe", "unsubscribe" loop will happen.
     */
    @Test
    fun `test - returned state flow is same instance`() = runComposeUiTest {

        data class TestKey(val id: Any) : State.Key<State?> {
            override val default: State? = null
        }

        setContent {
            installStates(States()) {
                assertSame(TestKey(42).composeFlow(), TestKey(42).composeFlow())
                assertNotEquals(TestKey(42).composeFlow(), TestKey(2411).composeFlow())
                assertNotEquals(TestKey(1).composeFlow(), TestKey(2).composeFlow())
            }
        }
    }

    @Test
    fun `integration test - returned state flow is same instance`() = runComposeUiTest {
        data class TestState(val id: Int) : State

        data class TestKey(val id: Int) : State.Key<TestState?> {
            override val default: TestState? = null
        }

        val states = States()

        var launchStateInvocations = 0

        CoroutineScope(states + Dispatchers.Unconfined).launchState { key: TestKey ->
            assertEquals(0, launchStateInvocations, "Expected 'launchState' to be called only once!")
            launchStateInvocations++
            TestState(key.id + launchStateInvocations).emit()
        }

        setContent {
            installStates(states) {
                Text(TestKey(42).composeValue()?.id?.toString() ?: "null", Modifier.testTag("text"))
            }
        }

        onNodeWithTag("text").assertTextEquals("43")
        waitForIdle()
        onNodeWithTag("text").assertTextContains("43")
    }
}