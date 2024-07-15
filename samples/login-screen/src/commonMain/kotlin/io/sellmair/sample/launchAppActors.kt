package io.sellmair.sample

import io.sellmair.sample.loginScreen.launchLoginScreenActor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchAppActors() = launch {
    launchUserStateActor()
    launchLoginScreenActor()
}
