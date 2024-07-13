package io.sellmair.jokes.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sellmair.jokes.LikeDislikeEvent

@Composable
fun MainPage() = Box(
    Modifier.fillMaxSize(),
) {
    /* Place the joke counter */
    Column(Modifier.align(Alignment.TopCenter).padding(top = 64.dp)) {
        Row(
            Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            JokeCounter()
        }
    }

    /* Place the joke */
    Row(
        Modifier.fillMaxWidth()
            .padding(12.dp)
            .align(Alignment.Center),
        horizontalArrangement = Arrangement.Center
    ) {
        JokeCard()
    }

    /* Place the Like/Dislike buttons */
    Row(
        Modifier.fillMaxWidth().animateContentSize()
            .align(Alignment.BottomCenter)
            .padding(bottom = 64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            LikeDislikeButton(LikeDislikeEvent.Rating.Dislike) { Text("Dislike") }
        }

        Column {
            LikeDislikeButton(LikeDislikeEvent.Rating.Like) { Text("Like") }
        }
    }
}
