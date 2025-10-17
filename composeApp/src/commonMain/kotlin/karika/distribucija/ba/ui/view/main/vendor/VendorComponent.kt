package karika.distribucija.ba.ui.view.main.vendor

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.VendorRepository
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {
    private val repository = VendorRepository()

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors = _vendors.asStateFlow()
    var sort = mutableStateOf("A - Z")
    val searchText = mutableStateOf("")
    val showFilter = mutableStateOf(false)
    val selectedRegion = mutableStateOf<List<KarikaUnit>>(listOf())

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            repository.vendors(
                currentPage = currentPage,
                pageSize = pageSize,
                filterValue = selectedRegion.value.takeIf { it.isNotEmpty() }
                    ?.let { selectedRegion.value.joinToString(",") { it.unit() } } ?: "",
                searchText = searchText.value,
                sortType = if (sort.value.startsWith("A")) "ASC" else "DESC"
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        _vendors.update {
                            if (reset) {
                                result.data
                            } else {
                                it + result.data
                            }
                        }
                        hasNextPage = result.data.size == pageSize
                        currentPage++
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