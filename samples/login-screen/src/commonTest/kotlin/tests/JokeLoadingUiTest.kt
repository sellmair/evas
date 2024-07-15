package tests

import androidx.compose.ui.test.*
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvas
import io.sellmair.jokes.CurrentJokeState
import io.sellmair.jokes.ui.MainPage
import io.sellmair.jokes.ui.UiTags
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class JokeLoadingUiTest {

    @Test
    fun `joke loading - success`() = runComposeUiTest {
        val events = Events()
        val states = States()

        setContent {
            installEvas(events, states) {
                MainPage()
            }
        }


        states.setState(CurrentJokeState, CurrentJokeState.Loading)
        onNodeWithTag(UiTags.JokeLoadingSpinner.name).assertExists()
        onNodeWithTag(UiTags.JokeText.name).assertDoesNotExist()

        states.setState(CurrentJokeState, CurrentJokeState.Joke("Fantastic Joke!"))
        onNodeWithTag(UiTags.JokeLoadingSpinner.name).assertDoesNotExist()
        onNodeWithTag(UiTags.JokeText.name).assertExists().assertTextEquals("Fantastic Joke!")
    }

    @Test
    fun `joke loading - error`() = runComposeUiTest {
        val events = Events()
        val states = States()

        setContent {
            installEvas(events, states) {
                MainPage()
            }
        }

        states.setState(CurrentJokeState, CurrentJokeState.Loading)
        onNodeWithTag(UiTags.JokeLoadingSpinner.name).assertExists()
        onNodeWithTag(UiTags.JokeText.name).assertDoesNotExist()
        onNodeWithTag(UiTags.JokeErrorText.name).assertDoesNotExist()

        states.setState(CurrentJokeState, CurrentJokeState.Error("Sad Error"))
        onNodeWithTag(UiTags.JokeLoadingSpinner.name).assertDoesNotExist()
        onNodeWithTag(UiTags.JokeText.name).assertDoesNotExist()
        onNodeWithTag(UiTags.JokeErrorText.name).assertExists().assertTextContains("Error: Sad Error")
    }
}