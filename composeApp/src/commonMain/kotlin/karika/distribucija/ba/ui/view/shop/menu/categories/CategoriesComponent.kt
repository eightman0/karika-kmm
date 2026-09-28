package karika.distribucija.ba.ui.view.shop.menu.categories

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.CategoryRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface CategoriesComponent : ScreenComponent {
    /** The top-level categories, with "SVI PROIZVODI" first. */
    val categories: StateFlow<List<Category>>

    /** The top-level category whose subcategories are shown, or null for the top level. */
    val subCategory: StateFlow<Category?>

    fun onSelectCategory(category: Category?)

    fun showProducts(category: Category?)

    /** Back from the subcategories to the top level. */
    fun reset()

    fun mainBack()
}

class DefaultCategoriesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder), CategoriesComponent {

    private val categoryRepository = CategoryRepository()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories = _categories.asStateFlow()

    private val _subCategory = MutableStateFlow<Category?>(null)
    override val subCategory = _subCategory.asStateFlow()

    init {
        get()
    }

    fun get() {
        scope.launch {
            categoryRepository.get()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _categories.update {
                                result.data.childrenData.apply {
                                    add(
                                        0, Category(
                                            name = "SVI PROIZVODI",
                                            childrenData = mutableListOf()
                                        )
                                    )
                                }
                            }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    override fun onSelectCategory(category: Category?) {
        _subCategory.update { category }
    }

    override fun showProducts(category: Category?) {
        mainNavigate(MainConfig.CategoryProducts(category ?: return))
    }

    override fun reset() {
        _subCategory.value = null
    }
}