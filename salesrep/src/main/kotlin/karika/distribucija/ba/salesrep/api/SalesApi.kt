package karika.distribucija.ba.salesrep.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.model.Category
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.Config
import karika.distribucija.ba.salesrep.model.Conversation
import karika.distribucija.ba.salesrep.model.DiscountRule
import karika.distribucija.ba.salesrep.model.DiscountRuleBody
import karika.distribucija.ba.salesrep.model.DiscountRuleSearchResults
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.model.NewCustomerRequest
import karika.distribucija.ba.salesrep.model.Notification
import karika.distribucija.ba.salesrep.model.OnBehalfCartItemInput
import karika.distribucija.ba.salesrep.model.OnBehalfCartItemRequest
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
import karika.distribucija.ba.salesrep.model.SendMessageRequest
import karika.distribucija.ba.salesrep.model.SendMessageResponse
import karika.distribucija.ba.salesrep.model.StaffRecipient
import karika.distribucija.ba.salesrep.model.StaffSendMessage
import karika.distribucija.ba.salesrep.model.StaffSendMessageRequest
import karika.distribucija.ba.salesrep.model.StaffStartThread
import karika.distribucija.ba.salesrep.model.StaffStartThreadRequest
import karika.distribucija.ba.salesrep.model.StaffThread
import karika.distribucija.ba.salesrep.model.StaffThreadMessageSearchResults
import karika.distribucija.ba.salesrep.model.StaffThreadSearchResults
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.model.VendorOperationsMe
import karika.distribucija.ba.salesrep.model.VendorOrder
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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

    /** GET /V1/vendor-operations/employees/{employeeId}/customers */
    suspend fun getEmployeeCustomers(
        employeeId: Long,
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/employees/$employeeId/customers")) {
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
    suspend fun requestPartnership(data: PartnershipRequestBody): Result<HttpResponse> =
        runCatching {
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
    suspend fun createCustomerDiscount(
        customerId: Long,
        data: DiscountRuleBody
    ): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers/$customerId/discounts")) {
                setBody(data)
            }
        }

    /** PUT /V1/vendor-operations/discounts/{ruleId} */
    suspend fun updateDiscount(ruleId: Long, data: DiscountRuleBody): Result<HttpResponse> =
        runCatching {
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
        search: String? = null,
        categoryId: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/products")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
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
            setBody(
                OnBehalfCartItemRequest(
                    OnBehalfCartItemInput(
                        sku = sku,
                        qty = qty,
                        discountPercent = discountPercent
                    )
                )
            )
        }
    }

    /** DELETE /V1/vendor-operations/customers/{customerId}/cart/items/{itemId} */
    suspend fun removeCartItem(customerId: Long, itemId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.delete(HttpClientProvider.url("vendor-operations/customers/$customerId/cart/items/$itemId"))
    }

    /** POST /V1/vendor-operations/customers/{customerId}/orders */
    suspend fun placeOnBehalfOrder(
        customerId: Long,
        message: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/customers/$customerId/orders")) {
            if (!message.isNullOrBlank()) {
                setBody(OnBehalfPlaceOrderRequest(OnBehalfPlaceRequestMessage(message)))
            }
        }
    }

    /** GET /V1/mobile/config - shipping provider price tables, for the Review screen's
     * delivery-price calculator. */
    suspend fun config(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/config"))
    }

    /** POST /V1/mobile/vendor/order/shippingDetails (multipart) - saves the rep-filled shipping
     * address/package form against a just-placed order. */
    suspend fun updateDelivery(data: VendorDeliveryServiceData): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("mobile/vendor/order/shippingDetails")) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("orderId", data.id)
                        append("address[contact_name]", data.name)
                        append("address[email]", data.email)
                        append("address[telephone]", data.telephone)
                        append("address[city]", data.city)
                        append("address[street]", data.street)
                        append("address[postcode]", data.postcode)
                        append("package[weight]", data.weight)
                        append("package[width]", data.width)
                        append("package[height]", data.height)
                        append("package[depth]", data.depth)
                        append("note", data.note)
                        append("company_code", data.companyCode)
                    },
                    boundary = "WebAppBoundary"
                )
            )
        }
    }

    /** GET /V1/mobile/vendor/order - full order detail (line items, address, commission). */
    suspend fun getOrder(orderId: String): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/vendor/order")) {
            parameter("orderId", orderId)
        }
    }

    /** POST /V1/mobile/vendor/order/items/change - edits a line item's qty/discount on a still
     * pending, unlocked order. [items] is a list of (itemId, qty, discountPercent) triples. */
    suspend fun updateOrder(
        orderId: String,
        items: List<Triple<String, String, String>>
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("mobile/vendor/order/items/change")) {
            setBody(
                JsonObject(
                    mapOf(
                        "orderId" to JsonPrimitive(orderId),
                        "items" to JsonObject(
                            mutableMapOf<String, JsonElement>().apply {
                                items.forEach {
                                    put(
                                        it.first,
                                        JsonObject(
                                            mapOf(
                                                "qty" to JsonPrimitive(it.second),
                                                "discount" to JsonPrimitive(it.third)
                                            )
                                        )
                                    )
                                }
                            }
                        )
                    )
                )
            )
        }
    }

    /** GET /V1/mobile/vendor/order/messages */
    suspend fun getOrderComments(orderId: String): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/vendor/order/messages")) {
            parameter("orderId", orderId)
        }
    }

    /** POST /V1/mobile/vendor/order/messages */
    suspend fun sendOrderComment(orderId: String, message: String): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(HttpClientProvider.url("mobile/vendor/order/messages")) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("orderId", orderId)
                            append("message", message)
                        }
                    )
                )
            }
        }

    /** GET /V1/mobile/vendor/order/pdf */
    suspend fun getOrderPdf(orderId: String): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/vendor/order/pdf")) {
            parameter("orderId", orderId)
        }
    }

    /** GET /V1/categories - shop-wide category tree, same shared endpoint composeApp's
     * CategoryApi uses (not vendor/customer-scoped). */
    suspend fun getCategories(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("categories"))
    }

    /** GET /V1/vendor-operations/conversations - internal staff-to-staff message threads. */
    suspend fun listConversations(page: Int = 1, pageSize: Int = 50): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/conversations")) {
                parameter("searchCriteria[current_page]", page)
                parameter("searchCriteria[page_size]", pageSize)
            }
        }

    /** POST /V1/vendor-operations/conversations */
    suspend fun startConversation(counterpartEmployeeId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/conversations")) {
            setBody(StaffStartThread(StaffStartThreadRequest(counterpartEmployeeId)))
        }
    }

    /** GET /V1/vendor-operations/conversations/recipients */
    suspend fun getConversationRecipients(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/conversations/recipients"))
    }

    /** GET /V1/vendor-operations/conversations/{threadId}/messages */
    suspend fun getConversationMessages(
        threadId: Long,
        page: Int = 1,
        pageSize: Int = 100
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/conversations/$threadId/messages")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
        }
    }

    /** POST /V1/vendor-operations/conversations/{threadId}/messages */
    suspend fun sendConversationMessage(threadId: Long, message: String): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/conversations/$threadId/messages")) {
                setBody(StaffSendMessage(StaffSendMessageRequest(message)))
            }
        }

    /** POST /V1/vendor-operations/conversations/{threadId}/read */
    suspend fun markConversationRead(threadId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/conversations/$threadId/read"))
    }

    /** GET /V1/mobile/message/list - customer/admin message threads (the same endpoint serves
     * both areas, differentiated only by the admin query param). */
    suspend fun listMessages(admin: Boolean): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/message/list")) {
            parameter("pageSize", 1000)
            parameter("curPage", 1)
            parameter("admin", admin)
        }
    }

    /** GET /V1/mobile/message/thread */
    suspend fun getMessageThread(threadId: String?, admin: Boolean): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.get(HttpClientProvider.url("mobile/message/thread")) {
                parameter("threadId", threadId)
                parameter("admin", admin)
            }
        }

    /** POST /V1/mobile/message/send (multipart; appends a files[] part when request.file is set). */
    suspend fun sendMessage(request: SendMessageRequest): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("mobile/message/send")) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("send_to_admin", request.sendToAdmin)
                        append("message", request.message)
                        append("subject", request.subject ?: "")
                        request.receiverId?.let { append("receiver_id", it) }
                        request.threadId?.let { append("thread_id", it) }
                        request.file?.let { (filename, bytes) ->
                            append(
                                "files[]",
                                bytes ?: return@let,
                                Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        if (filename?.endsWith(".pdf", ignoreCase = true) == true) "application/pdf" else "image/png"
                                    )
                                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                                }
                            )
                        }
                    },
                    boundary = "WebAppBoundary"
                )
            )
        }
    }

    /** POST /V1/mobile/message/markAsRead */
    suspend fun markMessageRead(threadId: String?): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("mobile/message/markAsRead")) {
            parameter("threadId", threadId)
        }
    }

    /** GET /V1/mobile/vendor/notifications */
    suspend fun notifications(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("mobile/vendor/notifications"))
    }

    /** POST /V1/mobile/vendor/notification/mark_read */
    suspend fun markNotificationRead(notificationId: String): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("mobile/vendor/notification/mark_read")) {
            parameter("notificationId", notificationId)
        }
    }
}

class SalesRepository internal constructor() {

    companion object {
        private const val TAG = "SalesRepository"
    }

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
            AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getEmployeeCustomers(
        employeeId: Long,
        page: Int,
        pageSize: Int = 10,
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
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
            val response =
                SalesApi().getInvitableCustomers(page, pageSize, search).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OperationalCustomerSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
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
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
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
                AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getProducts(
        page: Int,
        pageSize: Int = 20,
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
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
            val response =
                SalesApi().getPreviouslyOrderedProducts(customerId, page, pageSize, search)
                    .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfProductSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
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
            val response =
                SalesApi().addCartItem(customerId, sku, qty, discountPercent).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OnBehalfCartResponse>()))
                return@flow
            }
            emit(ResultState.Error("Greška pri ažuriranju korpe."))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun removeCartItem(customerId: Long, itemId: Long): Flow<ResultState<OnBehalfCartResponse>> =
        flow {
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
                AppLogger.e(TAG, "Network call failed", e)
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
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun config(): Flow<ResultState<Config>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().config().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Config>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateDelivery(data: VendorDeliveryServiceData): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().updateDelivery(data).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrder(orderId: String): Flow<ResultState<VendorOrder>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getOrder(orderId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorOrder>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateOrder(orderId: String, items: List<Triple<String, String, String>>): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().updateOrder(orderId, items).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrderComments(orderId: String): Flow<ResultState<List<Comment>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getOrderComments(orderId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Comment>>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun sendOrderComment(orderId: String, message: String): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().sendOrderComment(orderId, message).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrderPdfUrl(orderId: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getOrderPdf(orderId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().trim('"')))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    /** The response is a single root Category whose childrenData is the top-level list. */
    fun getCategories(): Flow<ResultState<Category>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getCategories().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Category>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun listConversations(): Flow<ResultState<StaffThreadSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().listConversations().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<StaffThreadSearchResults>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun startConversation(counterpartEmployeeId: Long): Flow<ResultState<StaffThread>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().startConversation(counterpartEmployeeId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<StaffThread>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getConversationRecipients(): Flow<ResultState<List<StaffRecipient>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().getConversationRecipients().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<StaffRecipient>>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getConversationMessages(threadId: Long): Flow<ResultState<StaffThreadMessageSearchResults>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().getConversationMessages(threadId).getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<StaffThreadMessageSearchResults>()))
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Network call failed", e)
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun sendConversationMessage(threadId: Long, message: String): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().sendConversationMessage(threadId, message).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
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
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun listMessages(admin: Boolean): Flow<ResultState<List<Conversation>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().listMessages(admin).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Conversation>>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    /** The response is an array wrapping one conversation; only its first inner message list
     * is used (matches composeApp's `result.data.firstOrNull()?.messages?.firstOrNull()`). */
    fun getMessageThread(threadId: String?, admin: Boolean): Flow<ResultState<List<Message>>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = SalesApi().getMessageThread(threadId, admin).getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    val conversations = response.body<List<Conversation>>()
                    emit(
                        ResultState.Success(
                            conversations.firstOrNull()?.messages?.firstOrNull()
                                ?: emptyList<Message>()
                        )
                    )
                    return@flow
                }
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Network call failed", e)
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun sendMessage(request: SendMessageRequest): Flow<ResultState<SendMessageResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().sendMessage(request).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<SendMessageResponse>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markMessageRead(threadId: String?): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().markMessageRead(threadId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun notifications(): Flow<ResultState<List<Notification>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().notifications().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Notification>>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markNotificationRead(notificationId: String): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = SalesApi().markNotificationRead(notificationId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
