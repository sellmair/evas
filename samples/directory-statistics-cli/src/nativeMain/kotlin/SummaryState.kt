import io.sellmair.evas.State
import io.sellmair.evas.collectEventsAsync
import io.sellmair.evas.launchStateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class SummaryState(
    val files: Int,
    val directories: Int,
    val size: Long,
) : State {
    companion object Key : State.Key<SummaryState> {
        override val default: SummaryState = SummaryState(files = 0, directories = 0, size = 0)
    }
}

fun CoroutineScope.launchSummaryStateActor() = launchStateProducer(SummaryState) {
    var statistics = SummaryState.default

    collectEventsAsync<DirectoryDiscoveredEvent> {
        statistics = statistics.copy(directories = statistics.directories + 1)
        statistics.emit()
    }

    collectEventsAsync<FileDiscoveredEvent> { event ->
        statistics = statistics.copy(
            files = statistics.files + 1,
            size = statistics.size + (event.metadata.size ?: 0)
        )

        statistics.emit()
    }
}