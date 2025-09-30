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

    private val _orders = MutableStateFlow<List<VendorOrder>>(emptyList())
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
                currentPage = currentPage,
                queryParams = queryParams
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _orders.update {
                            if (reset) {
                                result.data
                            } else {
                                it.plus(result.data)
                            }
                        }
                        currentPage++
                        hasNextPage = result.data.isNotEmpty()
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

        if (dateFrom.value.isNotEmpty() && dateTo.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][0][filters][0][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=from" +
                        "&searchCriteria[filterGroups][0][filters][0][value]=${dateFrom.value}" +
                        "&searchCriteria[filterGroups][0][filters][1][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][1][conditionType]=to" +
                        "&searchCriteria[filterGroups][0][filters][1][value]=${dateTo.value}"
            params.add(query)
        } else if (dateFrom.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][0][filters][0][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=from" +
                        "&searchCriteria[filterGroups][0][filters][0][value]=${dateFrom.value}" +
                        "&searchCriteria[filterGroups][0][filters][1][field]=created_at"
            params.add(query)
        } else if (dateTo.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][0][filters][0][field]=created_at" +
                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=to" +
                        "&searchCriteria[filterGroups][0][filters][0][value]=${dateTo.value}" +
                        "&searchCriteria[filterGroups][0][filters][1][field]=created_at"
            params.add(query)
        }

        if (orderNumber.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][0][field]=order_id" +
                        "&searchCriteria[filterGroups][0][filters][0][value]=${orderNumber.value}" +
                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=like"
            )
        }

        if (filterPriceFrom.value.isNotEmpty() && filterPriceTo.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][1][filters][0][field]=order_total" +
                        "&searchCriteria[filterGroups][1][filters][0][conditionType]=from" +
                        "&searchCriteria[filterGroups][1][filters][0][value]=${filterPriceFrom.value}" +
                        "&searchCriteria[filterGroups][1][filters][1][field]=order_total" +
                        "&searchCriteria[filterGroups][1][filters][1][conditionType]=to" +
                        "&searchCriteria[filterGroups][1][filters][1][value]=${filterPriceTo.value}"
            params.add(
                query
            )
        } else if (filterPriceFrom.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][1][filters][0][field]=order_total" +
                        "&searchCriteria[filterGroups][1][filters][0][conditionType]=from" +
                        "&searchCriteria[filterGroups][1][filters][0][value]=${filterPriceFrom.value}" +
                        "&searchCriteria[filterGroups][1][filters][1][field]=order_total"
            params.add(
                query
            )
        } else if (filterPriceTo.value.isNotEmpty()) {
            val query =
                "&searchCriteria[filterGroups][1][filters][0][field]=order_total" +
                        "&searchCriteria[filterGroups][1][filters][0][conditionType]=to" +
                        "&searchCriteria[filterGroups][1][filters][0][value]=${filterPriceTo.value}" +
                        "&searchCriteria[filterGroups][1][filters][1][field]=order_total"
            params.add(
                query
            )
        }

        if (payerName.value.isNotEmpty()) {
            params.add(
                "&searchCriteria[filterGroups][0][filters][0][field]=billing_name" +
                        "&searchCriteria[filterGroups][0][filters][0][value]=${payerName.value}" +
                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=like"
            )
        }

        //if (value.person.isNotEmpty()) {
        //    params.add(
        //        "&searchCriteria[filterGroups][0][filters][0][field]=b2b_pravno_lice" +
        //                "&searchCriteria[filterGroups][0][filters][0][value]=${value.person}" +
        //                "&searchCriteria[filterGroups][0][filters][0][conditionType]=like"
        //    )
        //}

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

    fun addSearchQuery() {
        queryParams = listOf(
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC",
            "&searchCriteria[filterGroups][0][filters][0][field]=b2b_pravno_lice" +
                    "&searchCriteria[filterGroups][0][filters][0][value]=${searchText.value}" +
                    "&searchCriteria[filterGroups][0][filters][0][conditionType]=like"
        )
    }
}