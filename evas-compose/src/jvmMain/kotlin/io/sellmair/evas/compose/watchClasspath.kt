package io.sellmair.evas.compose

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import kotlin.concurrent.thread
import kotlin.io.path.isDirectory

internal class ClasspathChange(
    val context: List<String>
)

private val logger = createLogger()

@OptIn(DelicateCoroutinesApi::class)
internal fun watchClasspath(classpath: List<Path>): Flow<ClasspathChange> {
    return channelFlow {
        val thread = thread(isDaemon = true, name = "Evas Classpath Watcher") {
            logger.debug("'Evas Classpath Watcher' started")
            try {
                val watchService = FileSystems.getDefault().newWatchService()
                classpath.forEach { path ->
                    if (path.isDirectory()) path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
                }

                while (isActive && !isClosedForSend) {
                    val key = watchService.take() ?: continue
                    try {
                        val change = ClasspathChange(key.pollEvents().map { it.context().toString() })
                        val failure = trySendBlocking(change).exceptionOrNull()
                        if (failure != null) break
                    } finally {
                        key.reset()
                    }
                }
            } catch (_: InterruptedException) {
                // Goodbye, my friend.
            }

            logger.debug("'Evas Classpath Watcher' finished")
        }

        awaitClose {
            logger.debug("'Evas Classpath Watcher': Sending close signal")
            thread.interrupt()
        }
    }
}


