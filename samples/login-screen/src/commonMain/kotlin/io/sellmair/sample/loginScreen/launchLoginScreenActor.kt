package io.sellmair.sample.loginScreen

import io.sellmair.evas.get
import io.sellmair.sample.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

fun CoroutineScope.launchLoginScreenActor() = launch {
    /* Starting those actors if necessary: When the user is not logged in, can also be done with a router */
    UserState.get().filterIsInstance<UserState.NotLoggedIn>().collectLatest {
        launchEmailStateActor()
        launchPasswordStateActor()
        launchUserLoginStateActor()
    }
}