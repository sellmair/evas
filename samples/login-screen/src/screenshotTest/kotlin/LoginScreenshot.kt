import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.sellmair.sample.ui.LoginScreen

class LoginScreenshot {
    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        installEvas()
        LoginScreen()
    }
}