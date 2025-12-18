package karika.distribucija.ba.ui.common.state.vendor

import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorSpecificHandler {
    private val dashRepository = DashRepository()
    private val _vendorDetails = MutableStateFlow(Vendor())
    val vendorDetails = _vendorDetails.asStateFlow()

    fun getVendorDetails(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.Main).launch {
            dashRepository.getProfile()
                .collect { result ->
                    callback.invoke()
                    if (result is ResultState.Success) {
                        _vendorDetails.update { result.data }
                    }
                }
        }
    }
}