package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    @SerialName("id") var id: Int? = null,
    @SerialName("group_id") var groupId: Int? = null,
    @SerialName("default_billing") var defaultBilling: String? = null,
    @SerialName("default_shipping") var defaultShipping: String? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("created_in") var createdIn: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("firstname") var firstname: String? = null,
    @SerialName("lastname") var lastname: String? = null,
    @SerialName("store_id") var storeId: Int? = null,
    @SerialName("website_id") var websiteId: Int? = null,
    @SerialName("addresses") var addresses: List<Address> = arrayListOf(),
    @SerialName("disable_auto_group_change") var disableAutoGroupChange: Int? = null,
    @SerialName("custom_attributes") var customAttributes: List<Attributes> = arrayListOf()
) {
    fun isEmpty() = id != null

    fun companyName() =
        customAttributes.find { it.attributeCode == "b2b_pravno_lice" }?.value ?: "-"

    fun address(): String {
        val address = addresses.firstOrNull() ?: return ""
        return "${address.street.firstOrNull()}, ${address.city}, ${address.postcode}, Bosna I Hercegovina, ${address.telephone}"
    }

    fun shippingAddresses(): List<Address> {
        return addresses.filter { it.defaultShipping == "true" }
    }

    fun billingAddress(): Address? {
        return addresses.firstOrNull { it.defaultBilling == "true" }
    }

    fun shippingAddress(): Address? {
        return addresses.firstOrNull { it.defaultShipping == "true" }
    }
}

@Serializable
data class Address(
    @SerialName("id") var id: Int? = null,
    @SerialName("customer_id") var customerId: Int? = null,
    @SerialName("country_id") var countryId: String? = null,
    @SerialName("street") var street: List<String> = arrayListOf(),
    @SerialName("telephone") var telephone: String? = null,
    @SerialName("postcode") var postcode: String? = null,
    @SerialName("city") var city: String? = null,
    @SerialName("firstname") var firstname: String? = null,
    @SerialName("lastname") var lastname: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("default_shipping") var defaultShipping: String? = null,
    @SerialName("default_billing") var defaultBilling: String? = null,
    @SerialName("save_in_address_book") var save: Int? = null,
) {
    fun address(): String {
        return "${street.firstOrNull()}, ${city}, ${postcode}, Bosna I Hercegovina, ${telephone}"
    }

}

@Serializable
data class Attributes(
    @SerialName("attribute_code")
    val attributeCode: String? = null,
    val value: String? = null
)


@Serializable
data class UpdateCustomerRequest(
    val customer: UserDetails
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
    val template: String
)

@Serializable
data class ChangePasswordRequest(
    val old_password: String,
    val password: String,
    val password_confirmation: String,
)

@Serializable
data class ChangePasswordResponse(
    val status: String? = null,
    val message: String? = null
)