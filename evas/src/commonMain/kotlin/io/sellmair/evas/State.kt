package io.sellmair.evas

import io.sellmair.evas.State.Key
import kotlinx.coroutines.flow.MutableStateFlow

public interface State {
    public interface Key<T : State?> {
        public val default: T
    }
}

internal sealed interface StateProducer {
    fun <T : State?> launchIfApplicable(key: Key<T>, state: MutableStateFlow<T>)
}