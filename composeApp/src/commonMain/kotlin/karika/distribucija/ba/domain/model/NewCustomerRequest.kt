package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewCustomerRequest(
    @SerialName("request") val request: NewCustomerRequestBody
)

@Serializable
data class NewCustomerRequestBody(
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
    @SerialName("country_id") val countryId: String,
    @SerialName("street") val street: List<String>,
    @SerialName("telephone") val telephone: String,
    @SerialName("postcode") val postcode: String,
    @SerialName("city") val city: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("default_billing") val defaultBilling: Boolean,
    @SerialName("default_shipping") val defaultShipping: Boolean
)

@Serializable
data class CustomAttribute(
    @SerialName("attribute_code") val attributeCode: String,
    @SerialName("value") val value: String
)
