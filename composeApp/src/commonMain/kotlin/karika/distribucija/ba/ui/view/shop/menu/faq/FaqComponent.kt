package karika.distribucija.ba.ui.view.shop.menu.faq

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.FaqRepository
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FaqComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val _faq = MutableStateFlow<List<Faq>>(emptyList())
    val faq = _faq.asStateFlow()

    init {
        get()
    }

    fun get() {
        scope.launch {
            FaqRepository().faq()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _faq.update {
                                result.data
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
}