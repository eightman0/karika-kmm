package karika.distribucija.ba.domain.model

import karika.distribucija.ba.ui.view.prelogin.registration.isShop
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class LoginDto(
    val username: String,
    val password: String,
    @Transient val userType: String = "shop"
) {
    fun getType() = if (userType.isShop()) "customer" else "vendor"
}
