package io.sellmair.evas.compose

import androidx.compose.runtime.*
import io.sellmair.evas.Events
import io.sellmair.evas.States

public val LocalStates: ProvidableCompositionLocal<States?> = staticCompositionLocalOf { null }

public val LocalEvents: ProvidableCompositionLocal<Events?> = staticCompositionLocalOf { null }

@Composable
public fun installStates(states: States, child: @Composable () -> Unit) {
    CompositionLocalProvider(LocalStates provides states, content = child)
}

@Composable
public fun installEvents(events: Events, child: @Composable () -> Unit) {
    CompositionLocalProvider(LocalEvents provides events, content = child)
}

@Composable
public fun events(): Events = LocalEvents.current ?: error("No events provided")

@Composable
public fun states(): States = LocalStates.current ?: error("No states provided")

