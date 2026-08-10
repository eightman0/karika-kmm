package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.Serializable

/**
 * Sales reps authenticate as vendor employees, so the login request always targets the
 * "vendor" integration token type (see composeApp's LoginDto.getType() - non-shop users get
 * "vendor"). Mirrors composeApp's LoginDto but drops the KarikaType field since this app only
 * ever logs in one way.
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
    val template: String
)
