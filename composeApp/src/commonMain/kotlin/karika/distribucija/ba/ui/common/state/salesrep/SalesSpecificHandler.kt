package karika.distribucija.ba.ui.common.state.salesrep

import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesSpecificHandler {
    private val salesRepository = SalesRepository()

    private val _me = MutableStateFlow(VendorOperationsMe())
    val me = _me.asStateFlow()

    /** `vendor_operations_enabled` from `me`: null while loading; false when the vendor has Vendor
     * Operations off or the `me` call failed - then every vendor-ops screen is hidden. */
    private val _vendorOpsEnabled = MutableStateFlow<Boolean?>(null)
    val vendorOpsEnabled = _vendorOpsEnabled.asStateFlow()

    /** Analytics needs `vendor_operations_enabled` + `can_see_own_analytics`. Null while `me` is
     * loading; a failed `me` call counts as no access. */
    private val _analyticsAccess = MutableStateFlow<Boolean?>(null)
    val analyticsAccess = _analyticsAccess.asStateFlow()

    val employeeId: Int
        get() = _me.value.employeeId?.toInt() ?: 0

    val vendorId: Int
        get() = _me.value.vendorId?.toInt() ?: 0

    /** Server-backed on-behalf cart, shared across catalog/cart/review screens. */
    val cart = MutableStateFlow<OnBehalfCartResponse?>(null)

    fun clearCartState() {
        cart.value = null
    }

    /** Suspends until `me` is loaded; analytics screens call this so nothing is fetched (and 403s)
     * before the capability is known, or at all without it. */
    suspend fun awaitAnalyticsAccess(): Boolean = analyticsAccess.filterNotNull().first()

    fun getMe(callback: () -> Unit = {}) {
        _vendorOpsEnabled.value = null
        _analyticsAccess.value = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                salesRepository.getMe().collect { result ->
                    if (result is ResultState.Success) {
                        _me.update { result.data }
                        _vendorOpsEnabled.value = result.data.vendorOperationsEnabled
                        _analyticsAccess.value = result.data.canSeeAnalytics
                    }
                    if (result is ResultState.Error) {
                        _vendorOpsEnabled.value = false
                        _analyticsAccess.value = false
                    }
                    callback.invoke()
                }
            } finally {
                if (_vendorOpsEnabled.value == null) _vendorOpsEnabled.value = false
                if (_analyticsAccess.value == null) _analyticsAccess.value = false
            }
        }
    }
}
