package karika.distribucija.ba.ui.view.main.menu.categories

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.CategoryRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.MainConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val categoryRepository = CategoryRepository()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _subCategory = MutableStateFlow<Category?>(null)
    val subCategory = _subCategory.asStateFlow()

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
                                            id = result.data.id,
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

    fun onSelectCategory(category: Category? = null) {
        _subCategory.update { category }
    }

    fun showProducts(category: Category?) {
        mainNavigate(MainConfig.CategoryProducts(category ?: return))
    }

    fun reset() {
        _subCategory.value = null
    }
}