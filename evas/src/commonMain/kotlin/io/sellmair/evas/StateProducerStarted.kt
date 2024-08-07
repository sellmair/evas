package io.sellmair.evas

public sealed class StateProducerStarted {
    public companion object {
        /**
         * A state-producing coroutine shall be started as soon as possible.
         */
        public val Eagerly: StateProducerStarted get() = StateProducerStartedEagerly

        /**
         * A state-producing coroutine shall only be started once at least one subscriber is present
         */
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