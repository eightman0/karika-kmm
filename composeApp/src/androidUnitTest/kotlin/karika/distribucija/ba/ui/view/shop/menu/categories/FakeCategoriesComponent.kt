package karika.distribucija.ba.ui.view.shop.menu.categories

import androidx.compose.material3.SnackbarHostState
import karika.distribucija.ba.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow

/** Moves between the levels like the real component, and records the rest. */
class FakeCategoriesComponent(categories: List<Category>) : CategoriesComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val categories = MutableStateFlow(categories)
    override val subCategory = MutableStateFlow<Category?>(null)

    val shownProducts = mutableListOf<Category>()
    var backRequests = 0

    override fun onSelectCategory(category: Category?) {
        subCategory.value = category
    }

    override fun showProducts(category: Category?) {
        shownProducts += category ?: return
    }

    override fun reset() {
        subCategory.value = null
    }

    override fun mainBack() {
        backRequests++
    }
}
