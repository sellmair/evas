package io.sellmair.evas.compose

import androidx.compose.runtime.*
import io.sellmair.evas.Events
import io.sellmair.evas.MissingEventsException
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
public fun installEvas(events: Events, states: States, child: @Composable () -> Unit) {
    installEvents(events) {
        installStates(states) {
            child()
        }
    }
}

@Composable
public fun eventsOrNull(): Events? = LocalEvents.current

@Composable
public fun eventsOrThrow(): Events = LocalEvents.current
    ?: throw MissingEventsException("Missing ${Events::class.simpleName} in composition tree")

@Composable
public fun statesOrNull(): States? = LocalStates.current

@Composable
public fun statesOrThrow(): States = LocalStates.current
    ?: throw MissingEventsException("Missing ${States::class.simpleName} in composition tree")




