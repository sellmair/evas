import io.sellmair.evas.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.lincheck.CTestConfiguration
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTestConfiguration
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.test.Test

class StatesLincheck {
    data class StateA(val value: Int) : State {
        companion object Key : State.Key<StateA?> {
            override val default = null
        }
    }

    data class StateB(val value: Int) : State {
        companion object Key : State.Key<StateB?> {
            override val default = null
        }
    }

    private val coldAProducer = ColdStateProducerImpl(
        CoroutineScope(Dispatchers.Default), StateA.Key::class,
        onInactive = OnInactive.keepValue(), onActive = {}
    )

    private val states = States()

    @Operation
    fun setStateA(value: Int) = states.setState(StateA, StateA(value))

    @Operation
    fun setStateB(value: Int) = states.setState(StateB, StateB(value))

    @Operation
    fun getStateA() = states.getState(StateA).value

    @Operation
    fun getStateB() = states.getState(StateB).value

    @Operation
    fun registerProducer() = (states as StatesImpl).registerProducer(coldAProducer)

    @Operation
    fun unregisterProducer() = (states as StatesImpl).unregisterProducer(coldAProducer)

    @Operation
    fun launchAProducer(value: Int): Unit = runBlocking(states) {
        launchState(StateA) {
            StateA(value).emit()
        }
    }

    @Operation
    fun launchBProducer(value: Int): Unit = runBlocking(states) {
        launchState(StateB) {
            StateB(value).emit()
        }
    }

    @Test
    fun stressTest() = StressOptions()
        .invocationsPerIteration(StressCTestConfiguration.DEFAULT_INVOCATIONS / 4)
        .iterations(CTestConfiguration.DEFAULT_ITERATIONS / 4)
        .check(this::class)

}