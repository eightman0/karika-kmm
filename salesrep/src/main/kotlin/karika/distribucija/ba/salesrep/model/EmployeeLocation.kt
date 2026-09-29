package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeLocationSubmit(
    @SerialName("device_id") val deviceId: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("accuracy") val accuracy: Double? = null,
    @SerialName("altitude") val altitude: Double? = null,
    @SerialName("speed") val speed: Double? = null,
    @SerialName("heading") val heading: Double? = null,
    @SerialName("timestamp") val timestamp: String? = null,
)

@Serializable
data class EmployeeLocationSubmitBody(
    @SerialName("location") val location: EmployeeLocationSubmit,
)
