package io.sellmair.jokes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.sellmair.evas.compose.composeValue
import io.sellmair.jokes.JokeCounterState

@Composable
fun JokeCounter() {
    Column(horizontalAlignment = CenterHorizontally) {
        Row {
            Text(
                "Jokes read today",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Row {
            Text(
                JokeCounterState.composeValue().jokesCount.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Thin,
                modifier = Modifier.testTag(UiTags.JokeCounterText.name)
            )
        }
    }
}