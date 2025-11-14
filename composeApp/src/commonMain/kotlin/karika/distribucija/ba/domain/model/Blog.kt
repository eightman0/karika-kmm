package karika.distribucija.ba.domain.model

import karika.distribucija.ba.domain.HttpClientProvider.blogImage
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Blog(
    @SerialName("post_id") var id: String? = null,
    @SerialName("post_title") var title: String? = null,
    @SerialName("post_content") var content: String? = null,
    @SerialName("short_description") var desc: String? = null,
    @SerialName("image") var image: String? = null,
    @SerialName("url_key") var urlKey: String? = null,
    @SerialName("published_at") var date: String? = null,
) {
    fun image() = blogImage(image)
}