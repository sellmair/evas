package io.sellmair.sample.loginScreen

import io.sellmair.evas.State
import io.sellmair.evas.collectEvents
import io.sellmair.evas.launchStateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class EmailState(
    val email: String = "",
    val isValid: Boolean = false
) : State {
    companion object Key : State.Key<EmailState> {
        override val default: EmailState = EmailState()
    }
}

fun CoroutineScope.launchEmailStateActor() = launchStateProducer(EmailState, Dispatchers.Main.immediate) {
    EmailState.default.emit()

    val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}") // yes, I know.
    val whiteSpaceRegex = Regex("\\s")
    collectEvents<EmailChangedEvent> { event ->
        val sanitizedEmail = event.email.replace(whiteSpaceRegex, "")
        EmailState(email = sanitizedEmail, isValid = sanitizedEmail.matches(emailRegex)).emit()
    }
}