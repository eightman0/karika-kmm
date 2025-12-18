package karika.distribucija.ba.ui.view.distributer.board

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.DashboardData
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val _dash = MutableStateFlow(DashboardData())
    val dash = _dash.asStateFlow()

    fun dash() {
        scope.launch {
            DashRepository()
                .get()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _dash.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message ?: "")
                        }
                    }
                }
        }
    }
}