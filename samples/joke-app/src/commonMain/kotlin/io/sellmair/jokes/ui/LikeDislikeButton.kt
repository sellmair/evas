package io.sellmair.jokes.ui

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import io.sellmair.evas.compose.LaunchingEvents
import io.sellmair.evas.compose.collectAsValue
import io.sellmair.evas.emit
import io.sellmair.jokes.CurrentJokeState
import io.sellmair.jokes.LikeDislikeEvent


@Composable
fun LikeDislikeButton(
    rating: LikeDislikeEvent.Rating,
    content: @Composable () -> Unit
) {
    val currentJokeState = CurrentJokeState.collectAsValue()

    Button(
        /* Do not enable the button if there is no joke loaded */
        enabled = currentJokeState is CurrentJokeState.Joke,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = when (rating) {
                LikeDislikeEvent.Rating.Like -> Color.Green
                LikeDislikeEvent.Rating.Dislike -> Color.Red
            }
        ),
        onClick = LaunchingEvents {
            LikeDislikeEvent(rating).emit()
        },
        modifier = Modifier.testTag(
            when (rating) {
                LikeDislikeEvent.Rating.Like -> UiTags.LikeButton.name
                LikeDislikeEvent.Rating.Dislike -> UiTags.DislikeButton.name
            }
        ),
    ) {
        content()
    }
}
