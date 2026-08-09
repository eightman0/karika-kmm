package karika.distribucija.ba.salesrep.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.salesrep.model.DiscountRule
import karika.distribucija.ba.salesrep.model.DiscountRuleBody
import karika.distribucija.ba.salesrep.model.DiscountRuleSearchResults
import karika.distribucija.ba.salesrep.model.NewCustomerRequest
import karika.distribucija.ba.salesrep.model.OnBehalfCartItemRequest
import karika.distribucija.ba.salesrep.model.OnBehalfCartItemInput
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponse
import karika.distribucija.ba.salesrep.model.OnBehalfOrderResult
import karika.distribucija.ba.salesrep.model.OnBehalfOrderSearchResults
import karika.distribucija.ba.salesrep.model.OnBehalfPlaceOrderRequest
import karika.distribucija.ba.salesrep.model.OnBehalfPlaceRequestMessage
import karika.distribucija.ba.salesrep.model.OnBehalfProductSearchResults
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.model.OperationalCustomerSearchResults
import karika.distribucija.ba.salesrep.model.Partnership
import karika.distribucija.ba.salesrep.model.PartnershipRequestBody
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorOperationsMe
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Ported from composeApp's domain/api/SalesApi.kt. Covers login/dashboard/orders (phase 1) and
 * customers/discounts/partnerships (phase 2). Catalog/cart/messages endpoints are a follow-up.
 */
internal class SalesApi {

    /** GET /V1/vendor-operations/me */
    suspend fun getMe(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/me"))
    }

    /** GET /V1/vendor-operations/orders */
    suspend fun getOrders(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/orders")) {
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

    /** GET /V1/vendor-operations/customers */
    suspend fun getCustomers(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/customers")) {
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

    /** GET /V1/vendor-operations/employees/{employeeId}/customers */
    suspend fun getEmployeeCustomers(
        employeeId: Long,
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/employees/$employeeId/customers")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "company")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "like")
            }
        }
    }

    /** GET /V1/vendor-operations/customers/invitable */
    suspend fun getInvitableCustomers(
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/customers/invitable")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "company")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "like")
            }
        }
    }

    /** POST /V1/vendor-operations/partnerships/request */
    suspend fun requestPartnership(data: PartnershipRequestBody): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/partnerships/request")) {
            setBody(data)
        }
    }

    /** POST /V1/vendor-operations/customers */
    suspend fun createCustomer(data: NewCustomerRequest): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers")) {
            setBody(data)
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/discounts */
    suspend fun getCustomerDiscounts(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/customers/$customerId/discounts"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/discounts */
    suspend fun createCustomerDiscount(customerId: Long, data: DiscountRuleBody): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers/$customerId/discounts")) {
                setBody(data)
            }
        }

    /** PUT /V1/vendor-operations/discounts/{ruleId} */
    suspend fun updateDiscount(ruleId: Long, data: DiscountRuleBody): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.put(HttpClientProvider.url("vendor-operations/discounts/$ruleId")) {
            setBody(data)
        }
    }

    /** DELETE /V1/vendor-operations/discounts/{ruleId} */
    suspend fun deleteDiscount(ruleId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(HttpClientProvider.url("vendor-operations/discounts/$ruleId"))
    }

    /**
     * GET /V1/vendor-operations/products - reused for the discount form's item search instead of
     * the general shop-wide Product/Category search composeApp's form uses, since it's already
     * scoped to this vendor. Category-level discount targeting is a follow-up (product or "all
     * products" only for now).
     */
    suspend fun getProducts(
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/products")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "name")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "like")
                parameter("searchCriteria[filter_groups][0][filters][1][field]", "sku")
                parameter("searchCriteria[filter_groups][0][filters][1][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][1][condition_type]", "like")
            }
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/previously-ordered-products */
    suspend fun getPreviouslyOrderedProducts(
        customerId: Long,
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(
            HttpClientProvider.url("vendor-operations/customers/$customerId/previously-ordered-products")
        ) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "name")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "like")
                parameter("searchCriteria[filter_groups][0][filters][1][field]", "sku")
                parameter("searchCriteria[filter_groups][0][filters][1][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][1][condition_type]", "like")
            }
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/cart */
    suspend fun getCart(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/customers/$customerId/cart"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/cart/items */
    suspend fun addCartItem(
        customerId: Long,
        sku: String,
        qty: Int,
        discountPercent: Int? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers/$customerId/cart/items")) {
            setBody(OnBehalfCartItemRequest(OnBehalfCartItemInput(sku = sku, qty = qty, discountPercent = discountPercent)))
        }
    }

    /** DELETE /V1/vendor-operations/customers/{customerId}/cart/items/{itemId} */
    suspend fun removeCartItem(customerId: Long, itemId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(HttpClientProvider.url("vendor-operations/customers/$customerId/cart/items/$itemId"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/orders */
    suspend fun placeOnBehalfOrder(customerId: Long, message: String? = null): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers/$customerId/orders")) {
            if (!message.isNullOrBlank()) {
                setBody(OnBehalfPlaceOrderRequest(OnBehalfPlaceRequestMessage(message)))
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getEmployeeCustomers(
        employeeId: Long,
        page: Int,
        pageSize: Int = 10,
        search: String? = null
    ): Flow<ResultState<OperationalCustomerSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getEmployeeCustomers(employeeId, page, pageSize, search).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OperationalCustomerSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getInvitableCustomers(
        page: Int,
        pageSize: Int = 20,
        search: String? = null
    ): Flow<ResultState<OperationalCustomerSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getInvitableCustomers(page, pageSize, search).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OperationalCustomerSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun requestPartnership(data: PartnershipRequestBody): Flow<ResultState<Partnership>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().requestPartnership(data).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Partnership>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
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
            val bodyText = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
            val errorMsg = if (bodyText.contains("already exists")) {
                "Kupac sa ovim email-om već postoji."
            } else {
                "Došlo je do greške. Pokušajte ponovo!"
            }
            emit(ResultState.Error(errorMsg))
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun createCustomerDiscount(customerId: Long, data: DiscountRuleBody): Flow<ResultState<DiscountRule>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().createCustomerDiscount(customerId, data).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<DiscountRule>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateDiscount(ruleId: Long, data: DiscountRuleBody): Flow<ResultState<DiscountRule>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().updateDiscount(ruleId, data).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<DiscountRule>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun deleteDiscount(ruleId: Long): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().deleteDiscount(ruleId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getProducts(
        page: Int,
        pageSize: Int = 20,
        search: String? = null
    ): Flow<ResultState<OnBehalfProductSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getProducts(page, pageSize, search).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfProductSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getPreviouslyOrderedProducts(
        customerId: Long,
        page: Int,
        pageSize: Int = 20,
        search: String? = null
    ): Flow<ResultState<OnBehalfProductSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getPreviouslyOrderedProducts(customerId, page, pageSize, search).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfProductSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCart(customerId: Long): Flow<ResultState<OnBehalfCartResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getCart(customerId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfCartResponse>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun addCartItem(
        customerId: Long,
        sku: String,
        qty: Int,
        discountPercent: Int? = null
    ): Flow<ResultState<OnBehalfCartResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().addCartItem(customerId, sku, qty, discountPercent).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfCartResponse>()))
                return@flow
            }
            emit(ResultState.Error("Greška pri ažuriranju korpe."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun removeCartItem(customerId: Long, itemId: Long): Flow<ResultState<OnBehalfCartResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().removeCartItem(customerId, itemId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfCartResponse>()))
                return@flow
            }
            emit(ResultState.Error("Greška pri ažuriranju korpe."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun placeOrder(customerId: Long, message: String? = null): Flow<ResultState<OnBehalfOrderResult>> = flow {
        emit(ResultState.Loading)
        val api = SalesApi()
        try {
            // Re-validate the server cart isn't empty before placing.
            val cartResponse = api.getCart(customerId).getOrNoInternet()
            if (cartResponse.status == HttpStatusCode.OK) {
                if (cartResponse.body<OnBehalfCartResponse>().isEmpty) {
                    emit(ResultState.Error("Korpa je prazna."))
                    return@flow
                }
            }

            val orderResponse = api.placeOnBehalfOrder(customerId, message).getOrNoInternet()
            if (orderResponse.status == HttpStatusCode.OK) {
                emit(ResultState.Success(orderResponse.body<OnBehalfOrderResult>()))
                return@flow
            }
            emit(ResultState.Error("Greška pri kreiranju narudžbe."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
