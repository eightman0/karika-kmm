package karika.distribucija.ba.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class LoginDto(
    val username: String,
    val password: String,
    @Transient val userType: String = "customer"
)
