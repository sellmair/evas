package io.sellmair.evas

public data object OnInactive {
    public fun <K : State.Key<T>, T : State?> keepValue(): ColdStateProducer<K, T> = {}
    public fun <K : State.Key<T>, T : State?> resetValue(): ColdStateProducer<K, T> = { key -> key.default.emit() }
}