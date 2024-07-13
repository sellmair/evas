package io.sellmair.jokes

import io.sellmair.evas.Event

data class LikeDislikeEvent(
    val rating: Rating
) : Event {

    enum class Rating {
        Like,
        Dislike
    }
}