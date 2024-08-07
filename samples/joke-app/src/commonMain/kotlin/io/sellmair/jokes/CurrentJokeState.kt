package io.sellmair.jokes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.sellmair.evas.State
import io.sellmair.evas.events
import io.sellmair.evas.launchState
import io.sellmair.jokes.network.httpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate

sealed class CurrentJokeState : State {
    companion object Key : State.Key<CurrentJokeState?> {
        override val default: CurrentJokeState? = null
    }

    data object Loading : CurrentJokeState()
    data class Error(val message: String) : CurrentJokeState()
    data class Joke(val joke: String) : CurrentJokeState()
}

fun CoroutineScope.launchJokeLoadingState(): Job = launchState(CurrentJokeState) {
    suspend fun loadJoke() {
        CurrentJokeState.Loading.emit()

        val response = httpClient.get("https://icanhazdadjoke.com/") {
            accept(ContentType.Text.Plain)
        }

        if (!response.status.isSuccess()) {
            CurrentJokeState.Error(response.status.description + ": ${response.bodyAsText()}").emit()
            return
        }

        CurrentJokeState.Joke(response.bodyAsText()).emit()
    }

    /* Load the first initial joke immediately */
    loadJoke()

    /*
     Listen to 'Like' or 'Dislike' events and reload the joke;
     Conflated because we do not care about further like/dislike events until we finished loading
    */
    events<LikeDislikeEvent>().conflate().collect {
        loadJoke()
    }
}
