package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RegisterDto(
    val customer: Customer?,
    val password: String?
)

@Serializable
data class Customer(
    val email: String?,
    val firstname: String?,
    val lastname: String?,
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

@Serializable
data class VendorRegisterRequest(
    val customerGroup: String,
    val customersRegion: String,
    val entity: String,
    val canton: String,
    val municipality: String,
    val companyName: String,
    val pdvNumber: String,
    val idNumber: String,
    val email: String,
    val pass: String,
    val repeatPass: String,
    val firstname: String,
    val lastname: String,
    val phone: String
)

@Serializable
data class ConfirmRegistration(
    @SerialName("public_name")
    val publicName: String,
    val email: String,
    val password: String,
    @SerialName("user_type")
    val userType: String
)