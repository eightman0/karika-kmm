package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors composeApp's domain/model/Category.kt, minus the unused (dead-code) subtree id
 * collector - selecting a category filters by its single id, matching SalesOrderCatalogComponent. */
@Serializable
data class Category(
    val id: Int = 0,
    @SerialName("parent_id") val parentId: Int = 0,
    val name: String = "",
    val level: Int = 0,
    @SerialName("children_data") val childrenData: List<Category> = emptyList()
)
