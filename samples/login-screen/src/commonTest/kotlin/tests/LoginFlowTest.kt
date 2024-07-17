package tests

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvas
import io.sellmair.sample.UserState
import io.sellmair.sample.ui.App
import io.sellmair.sample.ui.Tags
import kotlin.test.Test

class LoginFlowTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test - login flow`() = runComposeUiTest {
        val events = Events()
        val states = States()

        setContent {
            installEvas(events, states) {
                App()
            }
        }

        states.setState(UserState, UserState.NotLoggedIn)
        onNodeWithTag(Tags.LoginScreen.EmailTextField.name).assertExists()
        onNodeWithTag(Tags.LoginScreen.PasswordTextField.name).assertExists()

        states.setState(UserState, UserState.LoggedIn("eva@sellmair.io", "<token>"))
        onNodeWithTag(Tags.LoginScreen.EmailTextField.name).assertDoesNotExist()
        onNodeWithTag(Tags.LoginScreen.PasswordTextField.name).assertDoesNotExist()

        onNodeWithTag(Tags.MainScreen.UserEmail.name).assertExists()
    }
}