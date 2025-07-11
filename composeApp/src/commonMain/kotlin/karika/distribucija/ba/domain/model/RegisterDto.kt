package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RegisterDto(
    val customer: Customer,
    val password: String
)

@Serializable
data class Customer(
    val email: String,
    val firstname: String,
    val lastname: String,
    val addresses: ArrayList<Addresses>,
    val customAttributes: List<CustomAttributes> = arrayListOf()
)

@Serializable
data class Addresses(
    val countryId: String? = null,
    val street: ArrayList<String> = arrayListOf(),
    val telephone: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val defaultShipping: Boolean? = null,
    val defaultBilling: Boolean? = null
)

@Serializable
data class CustomAttributes(
    @SerialName("attribute_code")
    val attributeCode: String? = null,
    val value: JsonElement? = null
)