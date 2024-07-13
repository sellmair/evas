package io.sellmair.jokes

import kotlinx.coroutines.CoroutineScope

fun CoroutineScope.launchStateActors() {
    launchJokeCounterActor()
    launchJokeLoadingActor()
}