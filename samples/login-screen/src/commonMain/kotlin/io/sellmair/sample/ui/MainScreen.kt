package io.sellmair.sample.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sellmair.evas.compose.LaunchingEvents
import io.sellmair.evas.compose.collectAsValue
import io.sellmair.evas.emit
import io.sellmair.sample.UserState
import io.sellmair.sample.loginScreen.UserLogoutEvent

@Composable
fun MainScreen() {
    val userState = UserState.collectAsValue() as? UserState.LoggedIn ?: return
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            elevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    modifier = Modifier.testTag(Tags.MainScreen.UserEmail.name),
                    text = userState.email,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 32.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth()
                .padding(32.dp),
            onClick = LaunchingEvents {
                UserLogoutEvent.emit()
            }
        ) {
            Text("Logout")
        }
    }
}