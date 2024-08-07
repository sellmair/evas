package io.sellmair.jokes

import kotlinx.coroutines.CoroutineScope

fun CoroutineScope.launchStates() {
    launchJokeCounterState()
    launchJokeLoadingState()
}