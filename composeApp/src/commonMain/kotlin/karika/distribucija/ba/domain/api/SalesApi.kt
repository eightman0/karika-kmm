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
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.domain.model.OnBehalfProductSearchResults
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.Partnership
import karika.distribucija.ba.domain.model.PartnershipRequestBody
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.StaffSendMessageRequest
import karika.distribucija.ba.domain.model.StaffStartThreadRequest
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
        search: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/employees/$employeeId/customers")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            if (!search.isNullOrBlank()) {
                parameter("searchCriteria[filter_groups][0][filters][0][field]", "company")
                parameter("searchCriteria[filter_groups][0][filters][0][value]", "%$search%")
                parameter("searchCriteria[filter_groups][0][filters][0][condition_type]", "like")
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
    suspend fun addCartItem(customerId: Long, sku: String, qty: Int): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(url("vendor-operations/customers/$customerId/cart/items")) {
                setBody(OnBehalfCartItemRequest(OnBehalfCartItemInput(sku = sku, qty = qty)))
            }
        }

    /** DELETE /V1/vendor-operations/customers/{customerId}/cart/items/{itemId} */
    suspend fun removeCartItem(customerId: Long, itemId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(url("vendor-operations/customers/$customerId/cart/items/$itemId"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/orders */
    suspend fun placeOnBehalfOrder(customerId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/customers/$customerId/orders"))
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

    /** GET /V1/vendor-operations/conversations */
    suspend fun listConversations(page: Int = 1, pageSize: Int = 50): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.get(url("vendor-operations/conversations")) {
                parameter("searchCriteria[current_page]", page)
                parameter("searchCriteria[page_size]", pageSize)
            }
        }

    /** POST /V1/vendor-operations/conversations */
    suspend fun startConversation(counterpartEmployeeId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/conversations")) {
            setBody(
                karika.distribucija.ba.domain.model.StaffStartThread(
                    StaffStartThreadRequest(
                        counterpartEmployeeId
                    )
                )
            )
        }
    }

    /** GET /V1/vendor-operations/conversations/recipients */
    suspend fun getConversationRecipients(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/conversations/recipients"))
    }

    /** GET /V1/vendor-operations/conversations/{threadId}/messages */
    suspend fun getConversationMessages(
        threadId: Long,
        page: Int = 1,
        pageSize: Int = 100
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/conversations/$threadId/messages")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
        }
    }

    /** POST /V1/vendor-operations/conversations/{threadId}/messages */
    suspend fun sendConversationMessage(threadId: Long, message: String): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(url("vendor-operations/conversations/$threadId/messages")) {
                setBody(
                    karika.distribucija.ba.domain.model.StaffSendMessage(
                        StaffSendMessageRequest(
                            message
                        )
                    )
                )
            }
        }

    /** POST /V1/vendor-operations/conversations/{threadId}/read */
    suspend fun markConversationRead(threadId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/conversations/$threadId/read"))
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

    fun placeOrder(
        customerId: Long,
        items: List<Pair<OnBehalfProduct, Int>>
    ): Flow<ResultState<OnBehalfOrderResult>> = flow {
        emit(ResultState.Loading)
        val api = SalesApi()
        try {
            // 1. GET existing server cart and DELETE all stale items
            val cartResponse = api.getCart(customerId).getOrNoInternet()
            if (cartResponse.status == HttpStatusCode.OK) {
                val existingCart = cartResponse.body<OnBehalfCartResponse>()
                for (cartItem in existingCart.items) {
                    api.removeCartItem(customerId, cartItem.itemId)
                }
            }

            // 2. POST each local item to server cart
            for ((product, qty) in items) {
                val addResponse = api.addCartItem(customerId, product.sku, qty).getOrNoInternet()
                if (addResponse.status != HttpStatusCode.OK) {
                    emit(ResultState.Error("Greška pri dodavanju artikla \"${product.name}\" u korpu."))
                    return@flow
                }
            }

            // 3. Place the order
            val orderResponse = api.placeOnBehalfOrder(customerId).getOrNoInternet()
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
        search: String? = null
    ): Flow<ResultState<OperationalCustomerSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getEmployeeCustomers(employeeId, page, pageSize, search)
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

    fun listConversations(): Flow<ResultState<karika.distribucija.ba.domain.model.StaffThreadSearchResults>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().listConversations().getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<karika.distribucija.ba.domain.model.StaffThreadSearchResults>()))
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun startConversation(counterpartEmployeeId: Long): Flow<ResultState<karika.distribucija.ba.domain.model.StaffThread>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().startConversation(counterpartEmployeeId).getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<karika.distribucija.ba.domain.model.StaffThread>()))
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun getConversationRecipients(): Flow<ResultState<List<karika.distribucija.ba.domain.model.StaffRecipient>>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().getConversationRecipients().getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<List<karika.distribucija.ba.domain.model.StaffRecipient>>()))
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun getConversationMessages(threadId: Long): Flow<ResultState<karika.distribucija.ba.domain.model.StaffThreadMessageSearchResults>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().getConversationMessages(threadId).getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<karika.distribucija.ba.domain.model.StaffThreadMessageSearchResults>()))
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun sendConversationMessage(
        threadId: Long,
        message: String
    ): Flow<ResultState<karika.distribucija.ba.domain.model.StaffThreadMessage>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().sendConversationMessage(threadId, message).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<karika.distribucija.ba.domain.model.StaffThreadMessage>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markConversationRead(threadId: Long): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().markConversationRead(threadId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error(""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
