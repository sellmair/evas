package io.sellmair.jokes.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sellmair.evas.compose.composeFlow
import io.sellmair.evas.compose.composeValue
import io.sellmair.evas.flow
import io.sellmair.evas.value
import io.sellmair.jokes.CurrentJokeState

@Composable
fun JokeCard(modifier: Modifier = Modifier) {
    Card(
        elevation = 12.dp,
        modifier = modifier
            .padding(12.dp)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val currentJokeState = CurrentJokeState.composeValue()) {
                is CurrentJokeState.Error -> Text(
                    "Error: ${currentJokeState.message}",
                    Modifier.testTag(UiTags.JokeErrorText.name),
                )

                is CurrentJokeState.Loading -> CircularProgressIndicator(
                    Modifier.testTag(UiTags.JokeLoadingSpinner.name)
                )

                is CurrentJokeState.Joke -> Text(
                    currentJokeState.joke,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp,
                    modifier = Modifier.testTag(UiTags.JokeText.name)
                )

                null -> Unit
            }
        }
    }
}