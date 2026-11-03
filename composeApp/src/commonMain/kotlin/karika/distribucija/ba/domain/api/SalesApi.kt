package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.DiscountRuleBody
import karika.distribucija.ba.domain.model.DiscountRuleSearchResults
import karika.distribucija.ba.domain.model.ErrorResponse
import karika.distribucija.ba.domain.model.NewCustomerRequest
import karika.distribucija.ba.domain.model.OnBehalfCartItemInput
import karika.distribucija.ba.domain.model.OnBehalfCartItemRequest
import karika.distribucija.ba.domain.model.OnBehalfCartResponse
import karika.distribucija.ba.domain.model.OnBehalfOrderResult
import karika.distribucija.ba.domain.model.OnBehalfOrderSearchResults
import karika.distribucija.ba.domain.model.OnBehalfPlaceOrderRequest
import karika.distribucija.ba.domain.model.OnBehalfPlaceRequestMessage
import karika.distribucija.ba.domain.model.OnBehalfProductSearchResults
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.Partnership
import karika.distribucija.ba.domain.model.PartnershipRequestBody
import karika.distribucija.ba.domain.model.ReorderLine
import karika.distribucija.ba.domain.model.ReorderOutcome
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsError
import karika.distribucija.ba.domain.model.VendorOperationsMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class SalesApi {

    /** GET /V1/vendor-operations/me */
    suspend fun getMe(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/me"))
    }

    /** GET /V1/vendor-operations/customers */
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
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][value]",
                    "%$search%"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "like"
                )
                groupIdx++
            }

            if (status != null) {
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][field]",
                    "partnership_status"
                )
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "eq"
                )
            }
        }
    }

    /** POST /V1/vendor-operations/partnerships/request */
    suspend fun requestPartnership(data: PartnershipRequestBody): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(url("vendor-operations/partnerships/request")) {
                setBody(data)
            }
        }

    /** GET /V1/vendor-operations/customers/invitable */
    suspend fun getInvitableCustomers(
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/customers/invitable")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "company")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter(
                    "searchCriteria[filter_groups][0][filters][0][condition_type]",
                    "like"
                )
            }
        }
    }

    /** GET /V1/vendor-operations/employees/{employeeId}/customers */
    suspend fun getEmployeeCustomers(
        employeeId: Int,
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/employees/$employeeId/customers")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "company")
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][value]",
                    "%$search%"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "like"
                )
                groupIdx++
            }

            if (status != null) {
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][field]",
                    "partnership_status"
                )
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "eq"
                )
            }
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/discounts */
    suspend fun getCustomerDiscounts(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/customers/$customerId/discounts"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/discounts */
    suspend fun createCustomerDiscount(
        customerId: Long,
        data: DiscountRuleBody
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/customers/$customerId/discounts")) {
            setBody(data)
        }
    }

    /** PUT /V1/vendor-operations/discounts/{ruleId} */
    suspend fun updateDiscount(ruleId: Long, data: DiscountRuleBody): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.put(url("vendor-operations/discounts/$ruleId")) {
                setBody(data)
            }
        }

    /** POST /V1/vendor-operations/customers */
    suspend fun createCustomer(data: NewCustomerRequest): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/customers")) {
            setBody(data)
        }
    }

    /** DELETE /V1/vendor-operations/discounts/{ruleId} */
    suspend fun deleteDiscount(ruleId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(url("vendor-operations/discounts/$ruleId"))
    }

    /** GET /V1/vendor-operations/products */
    suspend fun getProducts(
        page: Int,
        pageSize: Int,
        search: String? = null,
        categoryId: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/products")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
                // OR: name like %search% OR sku like %search%
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "name")
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][value]",
                    "%$search%"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "like"
                )
                parameter("searchCriteria[filter_groups][$groupIdx][filters][1][field]", "sku")
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][1][value]",
                    "%$search%"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][1][condition_type]",
                    "like"
                )
                groupIdx++
            }

            if (categoryId != null) {
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][field]",
                    "category_id"
                )
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", categoryId)
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "in"
                )
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
            url("vendor-operations/customers/$customerId/previously-ordered-products")
        ) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                // OR: name like %search% OR sku like %search%
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "name")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter(
                    "searchCriteria[filter_groups][0][filters][0][condition_type]",
                    "like"
                )
                parameter("searchCriteria[filter_groups][0][filters][1][field]", "sku")
                parameter("searchCriteria[filter_groups][0][filters][1][value]", "%$search%")
                parameter(
                    "searchCriteria[filter_groups][0][filters][1][condition_type]",
                    "like"
                )
            }
        }
    }

    /** GET /V1/vendor-operations/customers/{customerId}/cart */
    suspend fun getCart(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/customers/$customerId/cart"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/cart/items */
    suspend fun addCartItem(
        customerId: Long,
        sku: String,
        qty: Int,
        discountPercent: Int? = null
    ): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(url("vendor-operations/customers/$customerId/cart/items")) {
                setBody(
                    OnBehalfCartItemRequest(
                        OnBehalfCartItemInput(sku = sku, qty = qty, discountPercent = discountPercent)
                    )
                )
            }
        }

    /** DELETE /V1/vendor-operations/customers/{customerId}/cart/items/{itemId} */
    suspend fun removeCartItem(customerId: Long, itemId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(url("vendor-operations/customers/$customerId/cart/items/$itemId"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/orders */
    suspend fun placeOnBehalfOrder(customerId: Long, message: String? = null): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(url("vendor-operations/customers/$customerId/orders")) {
                if (!message.isNullOrBlank()) {
                    setBody(OnBehalfPlaceOrderRequest(OnBehalfPlaceRequestMessage(message)))
                }
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
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][field]",
                    "increment_id"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][value]",
                    "%$search%"
                )
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "like"
                )
                groupIdx++
            }

            if (status != null) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "status")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter(
                    "searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]",
                    "eq"
                )
            }
        }
    }

    /** GET /V1/vendor-operations/orders filtered to one customer, newest first (no employeeId). */
    suspend fun getCustomerOrders(
        customerId: Long,
        page: Int,
        pageSize: Int
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/orders")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            parameter("searchCriteria[sort_orders][0][field]", "created_at")
            parameter("searchCriteria[sort_orders][0][direction]", "DESC")
            parameter("searchCriteria[filter_groups][0][filters][0][field]", "customer_id")
            parameter("searchCriteria[filter_groups][0][filters][0][value]", customerId.toString())
            parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "eq")
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

    fun getCustomerOrders(
        customerId: Long,
        page: Int,
        pageSize: Int = 12
    ): Flow<ResultState<OnBehalfOrderSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getCustomerOrders(customerId, page, pageSize).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfOrderSearchResults>()))
                return@flow
            }
            emit(ResultState.Error(response.vendorOperationsErrorMessage("Greška pri učitavanju narudžbi.")))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    /**
     * "Naruči ponovo": replaces the customer's on-behalf cart with [lines].
     * GET cart -> DELETE every line (stops on the first failure) -> POST each line without
     * discount_percent, skipping empty SKUs, qty <= 0 and failed adds.
     */
    suspend fun reorderIntoCart(customerId: Long, lines: List<ReorderLine>): ReorderOutcome =
        withContext(Dispatchers.Default) {
            if (lines.isEmpty()) {
                return@withContext ReorderOutcome.Failed("Nijedan artikal iz narudžbe nije dostupan.")
            }
            val api = SalesApi()
            try {
                // 1. Read the current cart.
                val cartResponse = api.getCart(customerId).getOrNoInternet()
                if (cartResponse.status != HttpStatusCode.OK) {
                    return@withContext ReorderOutcome.Failed(
                        cartResponse.vendorOperationsErrorMessage("Greška pri učitavanju korpe.")
                    )
                }
                val currentCart = cartResponse.body<OnBehalfCartResponse>()

                // 2. Clear it, one line at a time.
                for (item in currentCart.items) {
                    val deleteResponse = api.removeCartItem(customerId, item.itemId).getOrNoInternet()
                    if (deleteResponse.status != HttpStatusCode.OK) {
                        return@withContext ReorderOutcome.Failed(
                            deleteResponse.vendorOperationsErrorMessage("Greška pri pražnjenju korpe.")
                        )
                    }
                }

                // 3. Add each order line.
                val skipped = mutableListOf<String>()
                var lastCart: OnBehalfCartResponse? = null
                for (line in lines) {
                    val unavailable = "Proizvod ${line.name?.takeIf { it.isNotBlank() } ?: line.sku ?: ""} više nije dostupan."
                    val sku = line.sku?.trim()
                    if (sku.isNullOrEmpty() || line.qty <= 0) {
                        skipped += unavailable
                        continue
                    }
                    val addResponse = api.addCartItem(customerId, sku, line.qty).getOrNoInternet()
                    when {
                        addResponse.status == HttpStatusCode.OK ->
                            lastCart = addResponse.body<OnBehalfCartResponse>()

                        addResponse.vendorOperationsError()?.code == "customer_not_partnered" ->
                            return@withContext ReorderOutcome.Failed(
                                localizeVendorOperationsError("customer_not_partnered", null)
                            )

                        else -> skipped += unavailable
                    }
                }

                val cart = lastCart
                if (cart == null) {
                    ReorderOutcome.Failed(skipped.joinToString("\n"))
                } else {
                    ReorderOutcome.Added(cart, skipped)
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                ReorderOutcome.Failed(e.message ?: "Došlo je do greške. Pokušajte ponovo!")
            }
        }

    /**
     * Finds the [OperationalCustomer] with [customerId] in the caller's book (own customers, then
     * all vendor customers when allowed). Used by "Naruči ponovo" to open the cart review when the
     * order screen was not opened from the customer screen.
     */
    suspend fun findCustomer(
        customerId: Long,
        employeeId: Int,
        canSeeAllVendorCustomers: Boolean
    ): OperationalCustomer? = withContext(Dispatchers.Default) {
        val api = SalesApi()
        val pageSize = 100
        val maxPages = 20

        suspend fun scan(fetch: suspend (Int) -> HttpResponse): OperationalCustomer? {
            for (page in 1..maxPages) {
                val response = runCatching { fetch(page) }.getOrNull() ?: return null
                if (response.status != HttpStatusCode.OK) return null
                val result = runCatching { response.body<OperationalCustomerSearchResults>() }.getOrNull()
                    ?: return null
                result.items.firstOrNull { it.customerId == customerId }?.let { return it }
                if (result.items.size < pageSize || page * pageSize >= result.totalCount) return null
            }
            return null
        }

        try {
            (if (employeeId > 0) scan { api.getEmployeeCustomers(employeeId, it, pageSize).getOrNoInternet() } else null)
                ?: (if (canSeeAllVendorCustomers) scan { api.getCustomers(it, pageSize).getOrNoInternet() } else null)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

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
            val errorCode = runCatching { response.body<ErrorResponse>() }.getOrNull()
            val errorMsg = when (errorCode?.message) {
                "A customer with this email already exists. Use the partnership request flow to link to an existing customer." ->
                    "Kupac sa ovim email-om već postoji."

                else -> "Došlo je do greške. Pokušajte ponovo!"
            }
            emit(ResultState.Error(errorMsg))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun createCustomerDiscount(
        customerId: Long,
        data: DiscountRuleBody
    ): Flow<ResultState<DiscountRule>> = flow {
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

    fun updateDiscount(ruleId: Long, data: DiscountRuleBody): Flow<ResultState<DiscountRule>> =
        flow {
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
        pageSize: Int = 30,
        search: String? = null,
        categoryId: String? = null
    ): Flow<ResultState<OnBehalfProductSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response =
                SalesApi().getProducts(page, pageSize, search, categoryId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfProductSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getPreviouslyOrderedProducts(
        customerId: Long,
        page: Int,
        pageSize: Int = 30,
        search: String? = null
    ): Flow<ResultState<OnBehalfProductSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi()
                .getPreviouslyOrderedProducts(customerId, page, pageSize, search)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfProductSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("An error occurred. Please try again."))
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
            emit(ResultState.Error("An error occurred. Please try again."))
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

    fun placeOrder(
        customerId: Long,
        message: String? = null
    ): Flow<ResultState<OnBehalfOrderResult>> = flow {
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

    fun getEmployeeCustomers(
        employeeId: Int,
        page: Int,
        pageSize: Int = 20,
        search: String? = null,
        status: String? = null
    ): Flow<ResultState<OperationalCustomerSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response =
                SalesApi().getEmployeeCustomers(employeeId, page, pageSize, search, status)
                    .getOrNoInternet()
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

    fun getCustomerDiscounts(customerId: Long): Flow<ResultState<DiscountRuleSearchResults>> =
        flow {
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

private suspend fun HttpResponse.vendorOperationsError(): VendorOperationsError? =
    runCatching { body<VendorOperationsError>() }.getOrNull()

private suspend fun HttpResponse.vendorOperationsErrorMessage(fallback: String): String {
    val error = vendorOperationsError()
    return localizeVendorOperationsError(error?.code, error?.message?.takeIf { it.isNotBlank() } ?: fallback)
}

/** Bosnian message for a Vendor Operations `parameters.code`, falling back to [message]. */
internal fun localizeVendorOperationsError(code: String?, message: String?): String = when (code) {
    "customer_not_partnered" -> "Kupac nema aktivno partnerstvo sa vašom firmom."
    "product_not_in_catalog" -> "Proizvod nije pronađen u katalogu."
    "product_add_failed" -> "Proizvod nije moguće dodati u korpu."
    "cart_item_not_found" -> "Stavka korpe više ne postoji. Osvježite korpu i pokušajte ponovo."
    "forbidden" -> "Nemate dozvolu za ovu radnju."
    else -> message ?: "Došlo je do greške. Pokušajte ponovo!"
}
