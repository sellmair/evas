package io.sellmair.sample.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.sellmair.evas.compose.EvasLaunching
import io.sellmair.evas.compose.composeValue
import io.sellmair.evas.compose.rememberEvasCoroutineScope
import io.sellmair.evas.emit
import io.sellmair.sample.loginScreen.*
import io.sellmair.sample.ui.Tags.LoginScreen
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().imePadding().scrollable(scrollState, Orientation.Vertical)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        EmailPasswordCard()
    }
}

@Composable
fun EmailPasswordCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            LoginErrorText(Modifier.align(CenterHorizontally))

            EmailTextField()
            Spacer(Modifier.height(16.dp))

            PasswordTextField()
            Spacer(Modifier.height(16.dp))

            LoginButton(Modifier.align(CenterHorizontally))
        }
    }
}

@Composable
private fun EmailTextField() {
    val focusManager = LocalFocusManager.current

    val emailState = EmailState.composeValue()
    TextField(
        modifier = Modifier.fillMaxWidth()
            .testTag(LoginScreen.EmailTextField.name),
        value = emailState.email,
        isError = !emailState.isValid,
        label = { Text("Email") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        onValueChange = EvasLaunching { value ->
            EmailChangedEvent(value).emit()
        },
    )
}

@Composable
private fun PasswordTextField() {
    val focusManager = LocalFocusManager.current
    val passwordState = PasswordState.composeValue()
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberEvasCoroutineScope()
    TextField(
        modifier = Modifier.fillMaxWidth()
            .testTag(LoginScreen.PasswordTextField.name),
        value = passwordState.password,
        label = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { coroutineScope.launch { LoginClickedEvent.emit() } },
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        onValueChange = EvasLaunching { value ->
            PasswordChangedEvent(value).emit()
        }
    )
}

@Composable
private fun LoginButton(modifier: Modifier = Modifier) {
    val emailState = EmailState.composeValue()
    val passwordState = PasswordState.composeValue()
    val userLoginState = UserLoginState.composeValue()

    Button(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag(LoginScreen.LoginButton.name),
        onClick = EvasLaunching { LoginClickedEvent.emit() },
        enabled = emailState.isValid &&
                passwordState.password.isNotEmpty() &&
                userLoginState is UserLoginState.NotLoggedIn
    ) {
        Text("Login")
    }
}

@Composable
private fun LoginErrorText(modifier: Modifier = Modifier) {
    val notLoggedIn = UserLoginState.composeValue() as? UserLoginState.NotLoggedIn ?: return
    val error = notLoggedIn.error ?: return
    if (error.isEmpty()) return

    Text(
        modifier = modifier.testTag(LoginScreen.LoginErrorText.name),
        text = "Login failed: $error",
        color = MaterialTheme.colors.error,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))
}