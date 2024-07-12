package io.sellmair.evas

public data object OnInactive {
    public fun <K : State.Key<T>, T : State?> keepValue(): Producer<K, T> = {}
    public fun <K : State.Key<T>, T : State?> resetValue(): Producer<K, T> = { key -> key.default.emit() }
}