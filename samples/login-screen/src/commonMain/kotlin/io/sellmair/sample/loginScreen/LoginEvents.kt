package io.sellmair.sample.loginScreen

import io.sellmair.evas.Event

data class EmailChangedEvent(val email: String) : Event

data class PasswordChangedEvent(val password: String) : Event

data object LoginClickedEvent : Event

data object UserLogoutEvent : Event

data class UserLoginEvent(val email: String, val token: String) : Event

