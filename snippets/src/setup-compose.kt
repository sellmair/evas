import androidx.compose.runtime.Composable
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvas

//Start
val events = Events() // <- create new instance
val states = States() // <- create new instance

@Composable
fun App() {
    installEvas(events, states) {
        MainPage()
    }
}
//End

@Composable
fun MainPage() {

}