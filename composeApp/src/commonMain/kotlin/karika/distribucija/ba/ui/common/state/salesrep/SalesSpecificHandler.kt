package karika.distribucija.ba.ui.common.state.salesrep

import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesSpecificHandler {
    private val salesRepository = SalesRepository()

    private val _me = MutableStateFlow(VendorOperationsMe())
    val me = _me.asStateFlow()

    val employeeId: Int
        get() = _me.value.employeeId?.toInt() ?: 0

    val vendorId: Int
        get() = _me.value.vendorId?.toInt() ?: 0

    val salesRepCart = MutableStateFlow<Map<String, Pair<OnBehalfProduct, Int>>>(emptyMap())
    fun clearSalesRepCart() {
        salesRepCart.value = emptyMap()
    }

    fun getMe(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            salesRepository.getMe().collect { result ->
                callback.invoke()
                if (result is ResultState.Success) {
                    _me.update { result.data }
                }
            }
        }
    }
}
