
@file:Suppress("FunctionName")

package io.sellmair.evas

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlin.concurrent.Volatile

internal fun <T> AtomicSnapshotList(initial: MutableList<T> = mutableListOf()): AtomicSnapshot<MutableList<T>, List<T>> =
    AtomicSnapshot(initial, { it.toList() })

internal fun <K, V> AtomicSnapshotMap(initial: MutableMap<K, V> = mutableMapOf()): AtomicSnapshot<MutableMap<K, V>, Map<K, V>> =
    AtomicSnapshot(initial, { it.toMap() })

/**
 * Utility class for a lock-free read/write mechanism.
 * Reads will be performed by using an immutable [snapshot] of the current value (waiting for writes to have finished)
 * [write] functions have to be quick, as readers will just loop over the 'writing' bool
 */
internal class AtomicSnapshot<T, S>(
    private val value: T,
    private val createSnapshot: (T) -> S
) {
    private val writing = atomic(false)

    @Volatile
    private var snapshot = createSnapshot(value)

    fun write(body: (T) -> Unit) {
        while (true) {
            if (writing.compareAndSet(expect = false, update = true)) break
        }

        try {
            body(value)
        } finally {
            snapshot = createSnapshot(value)
            writing.value = false
        }
    }

    fun snapshot(): S {
        writing.loop { writing ->
            if (!writing) return snapshot
        }
    }
}

