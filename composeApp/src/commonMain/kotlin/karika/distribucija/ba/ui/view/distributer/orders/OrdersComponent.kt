package karika.distribucija.ba.ui.view.distributer.orders

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    private val _orders = MutableStateFlow<Set<VendorOrder>>(emptySet())
    val orders = _orders.asStateFlow()
    var filterPriceFrom = mutableStateOf("")
    var filterPriceTo = mutableStateOf("")
    var orderNumber = mutableStateOf("")
    var payerName = mutableStateOf("")
    var dateFrom = mutableStateOf("")
    var dateTo = mutableStateOf("")
    val showFilterState = mutableStateOf(false)
    val searchText = mutableStateOf("")
    private var queryParams =
        listOf("&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC")

    init {
        iOScope.launch {
            stateHolder.vendorNotificationHandler.notificationCount
                .collect {
                    loadNextPage(true)
                }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        if (reset) {
            hasNextPage = true
            currentPage = 1
        }

        if (!hasNextPage || loader.value) {
            return
        }

        iOScope.launch {
            DashRepository().getOrders(
                pageSize = pageSize,
                currentPage = currentPage,
                queryParams = queryParams
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _orders.update {
                            if (reset) {
                                result.data.toSet()
                            } else {
                                it.plus(result.data)
                            }
                        }
                        currentPage++
                        hasNextPage = result.data.size == pageSize
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message ?: "")
                    }
                }

            }
        }
    }

    fun filter() {
        val params = mutableListOf<String>()
        params.add(
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC"
        )

        var filterIndex = 0 // Brojač za filtere

        // Datum FROM
        if (dateFrom.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=from" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${dateFrom.value} 00:00:00"
            )
            filterIndex++
        }

        // Datum TO
        if (dateTo.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=to" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${dateTo.value} 23:59:59"
            )
            filterIndex++
        }

        // Order number
        if (orderNumber.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=order_id" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${orderNumber.value}" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=like"
            )
            filterIndex++
        }

        // Price FROM
        if (filterPriceFrom.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=order_total" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=from" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${filterPriceFrom.value}"
            )
            filterIndex++
        }

        // Price TO
        if (filterPriceTo.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=order_total" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=to" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${filterPriceTo.value}"
            )
            filterIndex++
        }

        // Payer name
        if (payerName.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=billing_name" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${payerName.value}" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=like"
            )
            filterIndex++
        }

        // B2B pravno lice (search)
        if (searchText.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][$filterIndex][field]=b2b_pravno_lice" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][value]=${searchText.value}" +
                        "&searchCriteria[filterGroups][0][filters][$filterIndex][conditionType]=like"
            )
        }

        queryParams = params
        loadNextPage(true)
    }

    fun hasFilter(): Boolean {
        return filterPriceTo.value.isNotEmpty() || filterPriceFrom.value.isNotEmpty() ||
                dateFrom.value.isNotEmpty() || dateTo.value.isNotEmpty() ||
                payerName.value.isNotEmpty() ||
                orderNumber.value.isNotEmpty()
    }

    fun clear() {
        filterPriceFrom.value = ""
        filterPriceTo.value = ""
        dateFrom.value = ""
        dateTo.value = ""
        payerName.value = ""
        orderNumber.value = ""

        filter()
    }
}