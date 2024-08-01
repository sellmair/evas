import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.collectEventsAsync
import io.sellmair.evas.emit
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath


fun main(args: Array<String>): Unit = runBlocking(Events() + States() + SupervisorJob()) {
    launchSummaryStateActor()
    launchInitialPathsStateActor()
    launchDiscoveryActors()
    launchUiActor()

    launch {
        args.forEach { arg ->
            InitialPathEvent(arg.toPath()).emit()
        }
    }

    collectEventsAsync<DiscoveryFinishedEvent> {
        coroutineContext.cancelChildren()
    }
}
