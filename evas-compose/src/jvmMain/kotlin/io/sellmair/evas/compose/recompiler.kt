package io.sellmair.evas.compose

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private val evasBuildRoot: String? = System.getProperty("evas.build.root")
private val evasBuildProject: String? = System.getProperty("evas.build.project")
private val evasBuildCompileTask: String? = System.getProperty("evas.build.compileTask")

private val logger = createLogger()

internal suspend fun launchRecompiler() {
    val evasBuildRoot = evasBuildRoot ?: run {
        logger.error("Missing 'evas.build.root' property")
        return
    }

    val evasBuildProject = evasBuildProject ?: run {
        logger.error("Missing 'evas.build.project' property")
        return
    }

    val evasBuildCompileTask = evasBuildCompileTask ?: run {
        logger.error("Missing 'evas.build.compile.task' property")
        return
    }

    val thread = thread(isDaemon = true, name = "Evas Recompiler") {
        logger.debug("'Evas Recompiler' started")
        val process = ProcessBuilder().directory(File(evasBuildRoot))
            .command("./gradlew", evasBuildProject + ":" + evasBuildCompileTask, "--console=plain", "-t")
            .inheritIO()
            .start()

        try {
            process.waitFor()
        } catch (_: InterruptedException) {
            process.destroy()
            if(!process.waitFor(15, TimeUnit.SECONDS)) {
                process.destroyForcibly()
            }
        }
        logger.debug("'Evas Recompiler' finished")
    }

    currentCoroutineContext().job.invokeOnCompletion {
        logger.debug("'Evas Recompiler': Sending close signal")
        thread.interrupt()
    }

    awaitCancellation()
}