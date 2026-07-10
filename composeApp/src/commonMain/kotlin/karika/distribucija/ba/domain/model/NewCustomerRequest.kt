package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewCustomerRequest(
    @SerialName("customer") val customer: NewCustomerPayload,
    @SerialName("auto_assign_to_caller") val autoAssignToCaller: Boolean? = null
)

@Serializable
data class NewCustomerPayload(
    @SerialName("email") val email: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("addresses") val addresses: List<NewCustomerAddress>,
    @SerialName("custom_attributes") val customAttributes: List<CustomAttribute>
)

@Serializable
data class NewCustomerAddress(
    @SerialName("street") val street: String,
    @SerialName("postcode") val postcode: String,
    @SerialName("telephone") val telephone: String,
    @SerialName("city") val city: String,
    @SerialName("country_id") val countryId: String = "BA"
)

@Serializable
data class CustomAttribute(
    @SerialName("attribute_code") val attributeCode: String,
    @SerialName("value") val value: String
)
