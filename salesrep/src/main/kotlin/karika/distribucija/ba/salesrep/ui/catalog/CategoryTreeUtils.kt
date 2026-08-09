package karika.distribucija.ba.salesrep.ui.catalog

import karika.distribucija.ba.salesrep.model.Category

/** Mirrors composeApp's SalesOrderCatalogView.kt private flattenToDepth() - flattens a category
 * tree up to [remaining] levels deep, pairing each category with its ancestor chain, for the
 * category sheet's flat deep-search and the active-category breadcrumb chip. */
data class FlatCategory(val category: Category, val ancestors: List<Category>)

fun flattenCategoriesToDepth(
    categories: List<Category>,
    parentAncestors: List<Category> = emptyList(),
    remaining: Int = 3
): List<FlatCategory> {
    if (remaining <= 0) return emptyList()
    return categories.flatMap { cat ->
        listOf(FlatCategory(cat, parentAncestors)) +
            if (cat.childrenData.isNotEmpty()) {
                flattenCategoriesToDepth(cat.childrenData, parentAncestors + cat, remaining - 1)
            } else emptyList()
    }
}

/** Full ancestor path (root-first) ending with [category] itself. */
fun categoryPath(allCategories: List<Category>, category: Category): List<Category> {
    val flat = flattenCategoriesToDepth(allCategories).find { it.category.id == category.id }
    return (flat?.ancestors ?: emptyList()) + category
}
