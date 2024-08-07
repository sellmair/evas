package io.sellmair.sample

import io.sellmair.sample.loginScreen.launchLoginScreenStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchAppStates() = launch {
    launchUserState()
    launchLoginScreenStates()
}
