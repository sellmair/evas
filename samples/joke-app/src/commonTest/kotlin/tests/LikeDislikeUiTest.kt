package tests

import androidx.compose.ui.test.*
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.collectEvents
import io.sellmair.evas.compose.installEvas
import io.sellmair.jokes.CurrentJokeState
import io.sellmair.jokes.LikeDislikeEvent
import io.sellmair.jokes.LikeDislikeEvent.Rating.Dislike
import io.sellmair.jokes.LikeDislikeEvent.Rating.Like
import io.sellmair.jokes.ui.MainPage
import io.sellmair.jokes.ui.UiTags
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LikeDislikeUiTest {
    @Test
    fun `like and dislike is only enabled when a joke is loaded`() = runComposeUiTest {
        val events = Events()
        val states = States()

        setContent {
            installEvas(events, states) {
                MainPage()
            }
        }

        onNodeWithTag(UiTags.LikeButton.name).assertExists().assertIsNotEnabled()
        onNodeWithTag(UiTags.DislikeButton.name).assertExists().assertIsNotEnabled()

        /* Set the current joke state to 'Loading' -> Still disabled */
        states.setState(CurrentJokeState, CurrentJokeState.Loading)
        onNodeWithTag(UiTags.LikeButton.name).assertExists().assertIsNotEnabled()
        onNodeWithTag(UiTags.DislikeButton.name).assertExists().assertIsNotEnabled()

        /* Set the current joke state to 'Joke' -> enabled */
        states.setState(CurrentJokeState, CurrentJokeState.Joke("Some Joke"))
        onNodeWithTag(UiTags.LikeButton.name).assertExists().assertIsEnabled()
        onNodeWithTag(UiTags.DislikeButton.name).assertExists().assertIsEnabled()

        /* Set the current joke state to 'Error' -> disabled */
        states.setState(CurrentJokeState, CurrentJokeState.Error("Some Error"))
        onNodeWithTag(UiTags.LikeButton.name).assertExists().assertIsNotEnabled()
        onNodeWithTag(UiTags.DislikeButton.name).assertExists().assertIsNotEnabled()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `pressing like`() = runComposeUiTest {
        val events = Events()
        val states = States()

        setContent {
            installEvas(events, states) {
                MainPage()
            }
        }

        /* Like, Dislike Buttons are only enabled if a joke is available */
        states.setState(CurrentJokeState, CurrentJokeState.Joke("Fantastic Joke!"))

        runBlocking(events + states) {
            /*
            Collect events:
            'UNDISPATCHED': Because we want to add the event listener asap, to not miss the first events
            'Unconfined': Because we want to receive the event from any thread, we do not want to wait for re-dispatching
             */
            val receivedEvents = mutableListOf<LikeDislikeEvent>()
            launch(start = CoroutineStart.UNDISPATCHED, context = Dispatchers.Unconfined) {
                collectEvents<LikeDislikeEvent> {
                    receivedEvents += it
                }
            }

            onNodeWithTag(UiTags.LikeButton.name).assertExists().assertIsEnabled().performClick()
            assertEquals(listOf(LikeDislikeEvent(Like)), receivedEvents)

            onNodeWithTag(UiTags.DislikeButton.name).assertExists().assertIsEnabled().performClick()
            assertEquals(listOf(LikeDislikeEvent(Like), LikeDislikeEvent(Dislike)), receivedEvents)

            coroutineContext.cancelChildren()
        }
    }
}