package io.sellmair.evas

import io.sellmair.evas.State.Key

/**
 * Marker interface for every state which can be held in [States]
 *
 * See: [States]
 * See: [States.Key]
 *
 * ## Example Usages
 * ### Define a data class a state. Typically, define the [Key] as companion.
 *
 * ```kotlin
 * data class MyNameState(val name: String): State {
 *     companion object Key: State.Key<MyNameState?> {
 *         val default = null
 *     }
 * }
 * ```
 *
 * ### State with 'dynamic keys'
 * ```kotlin
 * data class ProfileData(
 *     val id: UserId,
 *     val name: String
 *     val imageUrl: String,
 *     val followers: List<UserId>
 * ): State {
 *     data class Key(val id: UserId): State.Key<ProfileData?> {
 *         val default = null
 *     }
 * }
 * ```
 *
 * Such dynamic keys are used together with 'cold state producers'.
 *
 * ### Using dynamic keys with cold state producers
 * Using the `ProfileData` from the previous example:
 * ```kotlin
 * fun CoroutineScope.launchProfileDataLoader() = launchState(
 *    keepActive = 1.minutes
 * ) { key: ProfileData.Key ->
 *     val httpResponse = httpClient().requestProfileData(key.userId)
 *     ProfileData(
 *         id = key.userId,
 *         name = httpResponse.name,
 *         imageUrl = httpResponse.imageUrl,
 *         followers = httpResponse.followers
 *     ).emit()
 * }
 * ```
 *
 * This state producer will be launched once a request with the given Key was sent:
 *
 * ```kotlin
 * @Composable
 * fun ProfilePage(userId: UserId) {
 *     val profileData = ProfileData.Key(userId).composeValue()
 *     //                    ^
 *     //         Triggers the state producer to kick in (if not active already)
 *     // ...
 * }
 * ```
 */
public interface State {

    /**
     * See [State]
     * In 'evas', any State can be requested using a corresponding Key.
     * Requesting a state using a given key will trigger a given state producer to run and provide the state.
     *
     * ## Example Usages
     * See [State] for examples
     */
    public interface Key<T : State?> {
        public val default: T
    }
}
