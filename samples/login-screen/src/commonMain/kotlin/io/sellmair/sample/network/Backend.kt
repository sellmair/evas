package io.sellmair.sample.network

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

sealed class LoginResult {
data class Success(val token: String) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

fun Backend(): Backend = DummyBackend()

interface Backend {
    suspend fun login(email: String, password: String): LoginResult
}

/**
 * Dummy implementation for this sample
 */
private class DummyBackend : Backend {
    override suspend fun login(email: String, password: String): LoginResult {
        delay(Random.nextInt(500, 1500).milliseconds)

        return when {
            email == "eva@sellmair.io" && password == "Events and States" -> LoginResult.Success("Evas Token!")
            email == "other@sellmair.io" && password == "Other" -> LoginResult.Success("Other Token!")
            else -> LoginResult.Failure("Email or Password not correct")
        }
    }
}
