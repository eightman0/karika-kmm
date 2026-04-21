package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    var id: Int = 0,
    @SerialName("category_id")
    var categoryId: Int? = null,
    @SerialName("parent_id")
    var parentId: Int = 0,
    var name: String = "",
    @SerialName("is_active")
    var isActive: Boolean = false,
    var position: Int = 0,
    var level: Int = 0,
    @SerialName("product_count")
    var productCount: Int = 0,
    @SerialName("children_data")
    var childrenData: MutableList<Category> = mutableListOf()
) {
    private fun collectCategoryIds(category: Category): List<Category> {
        return if (category.childrenData.isNotEmpty()) {
            listOf(category) + category.childrenData.flatMap { collectCategoryIds(it) }
        } else {
            listOf(category)
        }
    }

    fun getAllCategoryIds(): String {
        return id.toString()

        //if (childrenData.isEmpty()) {
        //    return "$id"
        //}
        //return childrenData
        //    .flatMap { collectCategoryIds(it) }
        //    .joinToString(",") { it.id.toString() }
    }
}