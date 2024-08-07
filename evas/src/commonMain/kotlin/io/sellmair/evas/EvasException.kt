package io.sellmair.evas

/**
 * See
 * - [MissingEventsException]
 * - [MissingStatesException]
 */
public open class EvasException internal constructor(message: String) : Exception(message)

/**
 * Indicates that no [Events] were installed in the current context.
 *
 * For coroutines, consider bringing the events into the current context like
 * ```kotlin
 * withContext(events) {
 *           //  ^
 *           //  Or create a new instance with Events()
 * }
 * ```
 *
 * For compose, consider bringing the events into the current composition like
 * ```kotlin
 * installEvents(events) {
 *             //  ^
 *             //  Or create a new instance with Events()
 * }
 * ```
 */
public class MissingEventsException(message: String) : EvasException(message)


/**
 * Indicates that no [States] were installed in the current context.
 *
 * For coroutines, consider bringing the States into the current context like
 * ```kotlin
 * withContext(states) {
 *           //  ^
 *           //  Or create a new instance with States()
 * }
 * ```
 *
 * For compose, consider bringing the States into the current composition like
 * ```kotlin
 * installStates(states) {
 *             //  ^
 *             //  Or create a new instance with States()
 * }
 * ```
 */
public class MissingStatesException(message: String) : EvasException(message)