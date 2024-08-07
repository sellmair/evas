package tests

import androidx.compose.ui.test.*
import io.sellmair.evas.Events
import io.sellmair.evas.States
import io.sellmair.evas.compose.installEvas
import io.sellmair.sample.UserState
import io.sellmair.sample.loginScreen.UserLoginState
import io.sellmair.sample.loginScreen.launchLoginScreenStates
import io.sellmair.sample.ui.LoginScreen
import io.sellmair.sample.ui.Tags.LoginScreen
import kotlinx.coroutines.*
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoginScreenUiTest {
    @Test
    fun `test - login button is only enabled when email is valid and password is entered`() = runComposeUiTest {
        val events = Events()
        val states = States()
        setContent {
            installEvas(events, states) {
                LoginScreen()
            }
        }

        runBlocking(events + states + Job()) {
            /* Start the necessary actors we want to run for this test */
            launch(Dispatchers.Main.immediate) {
                launchLoginScreenStates()
            }

            states.setState(UserState, UserState.NotLoggedIn)

            onNodeWithTag(LoginScreen.LoginButton.name).assertExists().assertIsNotEnabled()

            // Enter E-Mail
            onNodeWithTag(LoginScreen.EmailTextField.name).assertExists()
                .performTextInput("eva@sellmair.io")
            onNodeWithTag(LoginScreen.LoginButton.name).assertExists().assertIsNotEnabled()

            // Enter Password
            onNodeWithTag(LoginScreen.PasswordTextField.name).assertExists()
                .performTextInput("<PASSWORD>")
            onNodeWithTag(LoginScreen.LoginButton.name).assertExists().assertIsEnabled()

            // Enter Invalid Email
            onNodeWithTag(LoginScreen.EmailTextField.name).assertExists()
                .performTextReplacement("notAnEmail")
            onNodeWithTag(LoginScreen.LoginButton.name).assertExists().assertIsNotEnabled()

            coroutineContext.cancelChildren()
        }
    }

    @Test
    fun `test - login failure is shown`() = runComposeUiTest {
        val events = Events()
        val states = States()
        setContent {
            installEvas(events, states) {
                LoginScreen()
            }
        }

        states.setState(UserLoginState, UserLoginState.NotLoggedIn())
        onNodeWithTag(LoginScreen.LoginErrorText.name).assertDoesNotExist()

        states.setState(UserLoginState, UserLoginState.NotLoggedIn("Error Code 420"))
        onNodeWithTag(LoginScreen.LoginErrorText.name).assertExists()
            .assertTextContains("Error Code 420", substring = true)
    }
}