package io.sellmair.jokes.ui

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.sellmair.evas.compose.collectAsValue
import io.sellmair.evas.compose.rememberEvasCoroutineScope
import io.sellmair.evas.emit
import io.sellmair.jokes.CurrentJokeState
import io.sellmair.jokes.LikeDislikeEvent
import kotlinx.coroutines.launch


@Composable
fun LikeDislikeButton(
    rating: LikeDislikeEvent.Rating,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberEvasCoroutineScope()
    val currentJokeState = CurrentJokeState.collectAsValue()

    Button(
        /* Do not enable the button if there is no joke loaded */
        enabled = currentJokeState is CurrentJokeState.Joke,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = when(rating) {
                LikeDislikeEvent.Rating.Like -> Color.Green
                LikeDislikeEvent.Rating.Dislike -> Color.Red
            }
        ),
        onClick = {
            coroutineScope.launch {
                LikeDislikeEvent(rating).emit()
            }
        }
    ) {
        content()
    }
}
