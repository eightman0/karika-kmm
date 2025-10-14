package karika.distribucija.ba.domain.model

import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    @SerialName("id") var id: Int? = null,
    @SerialName("entity_id") var entityId: Int = 0,
    @SerialName("entity_type_id") var entityTypeId: Int? = null,
    @SerialName("attribute_set_id") var attributeSetId: Int? = null,
    @SerialName("increment_id") var incrementId: String? = null,
    @SerialName("parent_id") var parentId: Int? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("website_id") var websiteId: Int? = null,
    @SerialName("address") var address: String? = null,
    @SerialName("city") var city: String? = null,
    @SerialName("zip_code") var zipCode: String? = null,
    @SerialName("customer_id") var customerId: Int? = null,
    @SerialName("shop_url") var shopUrl: String? = null,
    @SerialName("status") var status: String? = null,
    @SerialName("group") var group: String? = null,
    @SerialName("public_name") var publicName: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("company_logo") var companyLogo: String? = null,
    @SerialName("company_banner") var companyBanner: String? = null,
    @SerialName("country_id") var countryId: String? = null,
    @SerialName("b2b_vendor_grad") var b2bVendorGrad: String? = null,
    @SerialName("b2b_vendor_opicina") var b2bVendorOpicina: String? = null,
    @SerialName("b2b_vendor_kanton") var b2bVendorKanton: String? = null,
    @SerialName("b2b_vendor_pravno_lice") var b2bVendorPravnoLice: String? = null,
    @SerialName("b2b_vendor_pdv_broj") var b2bVendorPdvBroj: String? = null,
    @SerialName("b2b_vendor_id") var b2bVendorId: String? = null,
    @SerialName("b2b_vendor_phone") var b2bVendorPhone: String? = null,
    @SerialName("b2b_vendor_entitet") var b2bVendorEntitet: String? = null,
    @SerialName("b2b_target_customer_group") var b2bTargetCustomerGroup: String? = null,
    @SerialName("target_customer_region") var targetCustomerRegion: String? = null,
    @SerialName("promote_vendor_logo_from_date") var promoteVendorLogoFromDate: String? = null,
    @SerialName("promote_vendor_logo_to_date") var promoteVendorLogoToDate: String? = null,
    @SerialName("gender") var gender: Int? = null,
    @SerialName("vendor_bank_account_number") var bankAccountNumber: String? = null,
    @SerialName("vendor_min_order_amount") var minOrderAmount: String? = null,
    @SerialName("min_order_amount") var minOrderAmount1: String? = null,
    @SerialName("category_ids") var categories: List<String>? = null,
) {
    fun name() = publicName ?: ""
    fun image() = imageUrl(companyLogo)

    fun breadCrumbs(): String {
        return "Dobavljači > ${name()}"
    }

    fun minOrderAmount() = minOrderAmount ?: minOrderAmount1

    override fun hashCode(): Int = entityId.hashCode()

}