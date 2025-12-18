package karika.distribucija.ba.ui.view.main.menu.blog.overview

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlogOverviewComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    blog: Blog
) : CommonComponent(componentContext, stateHolder) {

    private val _blog = MutableStateFlow(blog)
    val blog = _blog.asStateFlow()

    init {
        get()
    }

    fun get() {
        scope.launch {
            userRepository.blog(blog.value.urlKey ?: "")
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _blog.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }
}