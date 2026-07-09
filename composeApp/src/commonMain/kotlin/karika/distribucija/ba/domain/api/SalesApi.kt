package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.DiscountRuleSearchResults
import karika.distribucija.ba.domain.model.NewCustomerRequest
import karika.distribucija.ba.domain.model.OnBehalfOrderSearchResults
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class SalesApi {

    /** GET /V1/vendor-operations/me */
    suspend fun getMe(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/me"))
    }

    /** GET /V1/vendor-operations/customers with optional server-side search + status filters */
    suspend fun getCustomers(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/customers")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "company")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]", "like")
                groupIdx++
            }

            if (status != null) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "partnership_status")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]", "eq")
            }
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/discounts */
    suspend fun getCustomerDiscounts(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/customers/$customerId/discounts"))
    }

    /** POST /V1/vendor-operations/customers */
    suspend fun createCustomer(data: NewCustomerRequest): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/customers")) {
            setBody(data)
        }
    }

    /** GET /V1/vendor-operations/orders */
    suspend fun getOrders(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/orders")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "increment_id")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]", "like")
                groupIdx++
            }

            if (status != null) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "status")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]", "eq")
            }
        }
    }
}

class SalesRepository internal constructor() {

    fun getMe(): Flow<ResultState<VendorOperationsMe>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getMe().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorOperationsMe>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCustomers(
        page: Int,
        pageSize: Int = 10,
        search: String? = null,
        status: String? = null
    ): Flow<ResultState<OperationalCustomerSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getCustomers(page, pageSize, search, status).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OperationalCustomerSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrders(
        page: Int,
        pageSize: Int = 10,
        search: String? = null,
        status: String? = null
    ): Flow<ResultState<OnBehalfOrderSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getOrders(page, pageSize, search, status).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfOrderSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun createCustomer(data: NewCustomerRequest): Flow<ResultState<OperationalCustomer>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().createCustomer(data).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OperationalCustomer>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCustomerDiscounts(customerId: Long): Flow<ResultState<DiscountRuleSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getCustomerDiscounts(customerId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<DiscountRuleSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
