import io.sellmair.evas.Events
import io.sellmair.evas.States

val events = Events() // <- create new instance
val states = States() // <- create new instance

@Composable
fun App() {
    installEvas(events, states) {
        MainPage()
    }
}