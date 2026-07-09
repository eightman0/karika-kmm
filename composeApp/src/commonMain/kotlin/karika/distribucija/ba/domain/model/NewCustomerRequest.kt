package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewCustomerRequest(
    @SerialName("customer") val customer: NewCustomerPayload
)

@Serializable
data class NewCustomerPayload(
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("email") val email: String,
    @SerialName("telephone") val telephone: String,
    @SerialName("company") val company: String,
    @SerialName("id_number") val idNumber: String,
    @SerialName("vat_number") val vatNumber: String? = null,
    @SerialName("street") val street: String,
    @SerialName("postcode") val postcode: String,
    /** "Federacija" | "Republika Srpska" | "Distrikt Brčko" */
    @SerialName("entity") val entity: String,
    /** Kanton (FBiH) or Općina (RS), null for Brčko */
    @SerialName("canton") val canton: String? = null,
    /** Grad — city name */
    @SerialName("city") val city: String? = null,
    @SerialName("store_size") val storeSize: String,
    @SerialName("store_type") val storeType: String,
    @SerialName("employee_count") val employeeCount: Int? = null
)
