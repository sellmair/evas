import io.sellmair.evas.Event
import okio.FileMetadata
import okio.Path

data class InitialPathEvent(val path: Path): Event

data class DirectoryDiscoveredEvent(val directory: Path): Event

data class FileDiscoveredEvent(
    val file: Path,
    val metadata: FileMetadata
): Event

data object DiscoveryFinishedEvent: Event
