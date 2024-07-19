package io.sellmair.evas

import kotlin.jvm.JvmStatic

public sealed class StateProducerStarted {
    public companion object {

        @JvmStatic
        public val Eagerly: StateProducerStarted get() = StateProducerStartedEagerly

        @JvmStatic
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