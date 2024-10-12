package io.sellmair.evas.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import java.io.File
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

    val gradleOutput = MutableStateFlow(listOf<String>())

    val gradleCompileWindow = thread {
        try {
            singleWindowApplication(
                title = "Evas Recompiler",
                state = WindowState(position = WindowPosition.Aligned(Alignment.BottomEnd)),
                exitProcessOnExit = false,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text("Recompiler", fontSize = 24.0f.sp, fontWeight = FontWeight.Bold)
                    }

                    Row {
                        Text("Save your code to recompile!", fontSize = 16.0f.sp)
                    }


                    Row {
                        Card(Modifier.padding(16.dp)) {
                            Box(Modifier.padding(16.dp)) {
                                val lines by gradleOutput.collectAsState()
                                val listState = LazyListState(lines.lastIndex)

                                LazyColumn(state = listState) {
                                    items(lines) { text ->
                                        Row {
                                            Text(text)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: InterruptedException) {

        }
    }

    val gradleCompileProcess = thread(name = "Evas Recompiler") {
        logger.debug("'Evas Recompiler' started")
        val process = ProcessBuilder().directory(File(evasBuildRoot))
            .command("./gradlew", "$evasBuildProject:$evasBuildCompileTask", "--console=plain", "--no-daemon", "-t")
            .apply {
                gradleOutput.update {
                    it + environment().entries.joinToString { (key, value) -> "$key=$value" }
                }

            }
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().use { reader ->
            while (true) {
                val nextLine = reader.readLine() ?: break
                logger.debug("'Evas Recompiler' output: $nextLine")
                gradleOutput.update { output -> (output + nextLine).takeLast(1024) }
            }
        }

        try {
            process.waitFor()
        } catch (_: InterruptedException) {
            logger.debug("'Evas Recompiler' interrupted. Destroying process")
            process.destroy()
        }
        logger.debug("'Evas Recompiler' finished")
    }

    currentCoroutineContext().job.invokeOnCompletion {
        logger.debug("'Evas Recompiler': Sending close signal")
        gradleCompileProcess.interrupt()
        gradleCompileWindow.interrupt()
    }

    awaitCancellation()
}