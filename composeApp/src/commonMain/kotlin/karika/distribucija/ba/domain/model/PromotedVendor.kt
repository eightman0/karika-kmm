package karika.distribucija.ba.domain.model

import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromotedVendor(
    @SerialName("promote_vendor_logo") var promoteVendorLogo: Boolean = false,
    @SerialName("promote_vendor_banner") var promoteVendorBanner: Boolean = false,
    @SerialName("entity_id") var entityId: String?,
    @SerialName("b2b_vendor_pravno_lice") var name: String?,
    @SerialName("shop_url") var shopUrl: String?,
    @SerialName("company_logo") var companyLogo: String?,
    @SerialName("company_banner") var companyBanner: String?,
    @SerialName("des") var description: String?,
    @SerialName("categories") var categories: List<String>?
) {
    fun name() = name ?: ""
    fun bannerImage() = imageUrl(companyBanner)
    fun logoImage() = imageUrl(companyLogo)
    fun toVendor() = Vendor(
        publicName = name(),
        entityId = entityId?.toIntOrNull() ?: 0,
        companyLogo = companyLogo,
        companyBanner = companyBanner
    )

    fun categories(categories: List<Category>): List<Category> {
        return categories.plus(categories.flatMap { it -> it.childrenData.flatMap { it -> it.childrenData.flatMap { it.childrenData } } })
            .filter {
                this.categories?.contains(it.id.toString()) ?: false
            }
    }
}