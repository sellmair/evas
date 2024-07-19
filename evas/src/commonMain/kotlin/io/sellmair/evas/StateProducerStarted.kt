package io.sellmair.evas

public sealed class StateProducerStarted {
    public companion object {
        public val Eagerly: StateProducerStarted get() = StateProducerStartedEagerly
        public val Lazily: StateProducerStarted get() = StateProducerStartedLazily
    }
}

/*
Explicitly not sharing the types:
Switching over this sealed class is not part of the public API.
 */
internal data object StateProducerStartedEagerly : StateProducerStarted()

/*
Explicitly not sharing the types:
Switching over this sealed class is not part of the public API.
 */
internal data object StateProducerStartedLazily : StateProducerStarted()