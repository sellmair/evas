import io.sellmair.evas.value
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

internal const val ansiReset = "\u001B[0m"
internal const val ansiCyan = "\u001B[36m"
internal const val ansiGreen = "\u001B[32m"

fun CoroutineScope.launchUiActor() = launch(Dispatchers.Default) {
    try {
        while (true) {
            printUI()
            delay(64.milliseconds)
        }
    } catch (_: CancellationException) {
        printUI()
    }
}

private suspend fun printUI() {
    print("\u001b[H\u001b[2J")

    val initialPaths = InitialPathsState.value()
    val statistics = SummaryState.value()

    if (initialPaths != null) {
        initialPaths.initialPaths.forEach { path ->
            println("$ansiCyan$path$ansiReset")
        }
        println()
    }

    println("${ansiGreen}Files Total:$ansiReset ${statistics.files + statistics.directories}")
    println("${ansiGreen}Directories:${ansiReset} ${statistics.directories}")
    println("${ansiGreen}Files:$ansiReset ${statistics.files}")
    println("${ansiGreen}Size:${ansiReset} ${statistics.size / 1_000_000} MB")
    println()
    println("#################################################")

    repeat(discoveryWorkers) { workerIndex ->
        val state = DiscoveryActorState.Key(workerIndex).value()
        println("# Discovery Worker: $workerIndex - ${state?.path?.toString()?.shorten() ?: "Idle"}")
    }
    println("#################################################")
}

private fun String.shorten(): String {
    if (length <= 120) return this
    return "${take(30)}...${takeLast(90)}"
}