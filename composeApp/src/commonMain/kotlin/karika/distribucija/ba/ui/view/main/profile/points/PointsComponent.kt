package karika.distribucija.ba.ui.view.main.profile.points

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.PointsRepository
import karika.distribucija.ba.domain.model.Bonus
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Transaction
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PointsComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {
    private val repository = PointsRepository()
    private val _points = MutableStateFlow(Bonus())
    val points = _points.asStateFlow()
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    init {
        get()
    }

    private fun get() {
        iOScope.launch {
            repository.get()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _points.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                        }
                    }
                }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        if (!hasNextPage) {
            return
        }

        iOScope.launch {
            repository.trx(currentPage = currentPage).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _transactions.update { it.plus(result.data) }
                        currentPage++
                        hasNextPage = result.data.isNotEmpty()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                    }
                }
            }
        }
    }
}