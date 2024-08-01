import io.sellmair.evas.State
import io.sellmair.evas.collectEvents
import io.sellmair.evas.launchStateProducer
import kotlinx.coroutines.CoroutineScope
import okio.Path

data class InitialPathsState(val initialPaths: List<Path>) : State {
    companion object Key : State.Key<InitialPathsState?> {
        override val default = null
    }
}

fun CoroutineScope.launchInitialPathsStateActor() = launchStateProducer(InitialPathsState) {
    var state = InitialPathsState(emptyList())
    state.emit()
    collectEvents<InitialPathEvent> { event ->
        state = state.copy(initialPaths = state.initialPaths + event.path)
        state.emit()
    }
}