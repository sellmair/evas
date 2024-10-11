import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvas
import io.sellmair.evas.compose.linkUI

fun main() {
    val events = Events()
    val states = States()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 600.dp, height = 800.dp)
        ) {

            installEvas(events, states) {
                linkUI("AppKt")
            }
        }
    }
}
