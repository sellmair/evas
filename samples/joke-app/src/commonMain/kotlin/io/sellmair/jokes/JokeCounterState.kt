package io.sellmair.jokes

import io.sellmair.evas.State
import io.sellmair.evas.collect
import io.sellmair.evas.launchState
import kotlinx.coroutines.CoroutineScope

data class JokeCounterState(val jokesCount: Int) : State {
    companion object Key : State.Key<JokeCounterState> {
        override val default: JokeCounterState = JokeCounterState(jokesCount = 0)
    }
}

fun CoroutineScope.launchJokeCounterState() = launchState(JokeCounterState) {
    var state = JokeCounterState.default

    CurrentJokeState.collect { currentJokeState ->
        if (currentJokeState is CurrentJokeState.Joke) {
            state = state.copy(jokesCount = state.jokesCount + 1)
            state.emit()
        }
    }
}