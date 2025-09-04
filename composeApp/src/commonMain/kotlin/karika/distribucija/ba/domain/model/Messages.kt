package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesCount(
    @SerialName("admin") var admin: String?,
    @SerialName("user") var user: String?,
) {
    fun total(): Int {
        return admin() + user()
    }

    fun admin(): Int {
        return (admin?.toIntOrNull() ?: 0)
    }

    fun user(): Int {
        return (user?.toIntOrNull() ?: 0)
    }
}