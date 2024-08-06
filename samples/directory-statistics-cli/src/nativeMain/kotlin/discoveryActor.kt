import io.sellmair.evas.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import okio.FileSystem
import okio.Path

const val discoveryWorkers = 12

class DiscoveryActorState(val path: Path) : State {
    data class Key(val workerIndex: Int) : State.Key<DiscoveryActorState?> {
        override val default: DiscoveryActorState? = null
    }
}

fun CoroutineScope.launchDiscoveryActors() = launch {
    val queue = Channel<Path>(UNLIMITED)
    var governor: Job? = null

    collectEvents<InitialPathEvent> { event ->
        queue.send(event.path)
        if (governor == null) {
            governor = launchDiscoveryGovernor(queue)
        }
    }
}

private fun CoroutineScope.launchDiscoveryGovernor(queue: Channel<Path>) = launch {
    val workers = arrayOfNulls<Job>(discoveryWorkers)
    val workerQueue = Channel<Path>(UNLIMITED)
    while (isActive) {
        val path = select<Path?> {
            queue.onReceive { it }
            workers.forEach { worker -> worker?.onJoin?.invoke { null } }
        }

        if (path == null && workers.all { it == null || !it.isActive }) {
            break
        }

        if (path != null) {
            workerQueue.send(path)
        }

        workers.forEachIndexed { workerIndex, job ->
            if (job == null || !job.isActive) {
                workers[workerIndex] = launchDiscoveryActor(workerIndex, receive = workerQueue, send = queue)
            }
        }
    }

    DiscoveryFinishedEvent.emit()
}


private fun CoroutineScope.launchDiscoveryActor(
    workerIndex: Int,
    receive: ReceiveChannel<Path>,
    send: SendChannel<Path>,
) = launch(Dispatchers.IO) {
    val fs = FileSystem.SYSTEM
    val stateKey = DiscoveryActorState.Key(workerIndex)

    while (isActive) {
        val path = receive.tryReceive().getOrNull() ?: break
        val metadata = fs.metadataOrNull(path) ?: continue
        if (metadata.isDirectory) {
            stateKey.set(DiscoveryActorState(path))
            DirectoryDiscoveredEvent(path).emitAsync()
            fs.listOrNull(path)?.forEach { child -> send.send(child) }
        } else if (metadata.isRegularFile) {
            FileDiscoveredEvent(path, metadata).emit()
        }

        stateKey.set(null)
    }
}