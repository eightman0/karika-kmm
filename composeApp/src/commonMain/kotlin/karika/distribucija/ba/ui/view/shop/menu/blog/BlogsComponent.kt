package karika.distribucija.ba.ui.view.shop.menu.blog

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlogsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val _blogs = MutableStateFlow<List<Blog>>(emptyList())
    val blogs = _blogs.asStateFlow()


    init {
        get()
    }

    fun get() {
        scope.launch {
            userRepository.blogs()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _blogs.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun navigateToBlog(blog: Blog) {
        appNavigate(AppConfig.Blog(blog))
    }
}