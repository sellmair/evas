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
    launchUiActor()
    launchSummaryState()
    launchInitialPathsState()
    launchDiscoveryActors()

    /* Send arguments, representing the entry points, as events */
    launch {
        args.forEach { arg ->
            InitialPathEvent(arg.toPath()).emit()
        }
    }

    /* Await the discovery of files to finish, cancel all children: Programm finished going over all files */
    collectEventsAsync<DiscoveryFinishedEvent> {
        coroutineContext.cancelChildren()
    }
}
