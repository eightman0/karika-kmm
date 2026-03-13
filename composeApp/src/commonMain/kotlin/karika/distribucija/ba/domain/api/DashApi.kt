package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.AIResponse
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.DashboardData
import karika.distribucija.ba.domain.model.MediaGallery
import karika.distribucija.ba.domain.model.Notification
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class DashApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(url("mobile/vendor/dashboard"))
    }

    suspend fun getOrders(
        pageSize: Int = 10,
        currentPage: Int,
        queryParams: List<String> = emptyList(),
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            StringBuilder(url("mobile/vendor/orders?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage"))
                .apply {
                    queryParams.forEach {
                        append(it)
                    }
                }.toString()
        )
    }

    suspend fun getOrder(
        id: String
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(url("mobile/vendor/order?orderId=$id"))
    }

    suspend fun getOrderComments(
        orderId: String
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(url("mobile/vendor/order/messages?orderId=$orderId"))
    }

    suspend fun sendComment(
        orderId: String,
        comment: String,
        attachment: ByteArray? = null,
        filename: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(url("mobile/vendor/order/messages")) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("orderId", orderId)
                        append("message", comment)
                        attachment?.let {
                            append("file", it, Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    ContentType.Application.Pdf.contentType
                                )
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"$filename\""
                                )
                            })
                        }
                    }.withLog(),
                    boundary = "WebAppBoundary"
                )
            )
        }
    }

    suspend fun changeOrderStatus(
        type: String,
        orderId: String,
        message: String,
        withDelivery: Boolean? = null,
        attachment: ByteArray? = null,
        filename: String? = null
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(url("mobile/vendor/order/$type")) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("orderId", orderId)
                        append("message", message)

                        withDelivery?.let {
                            append("approve-with-delivery", withDelivery)
                            append("approve-without-delivery", !withDelivery)
                        }
                        attachment?.let {
                            append("file", it, Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    ContentType.Application.Pdf.contentType
                                )
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"$filename\""
                                )
                            })
                        }
                    }.withLog(),
                    "WebAppBoundary"
                )
            )
        }
    }

    suspend fun createInvoice(
        orderId: String,
        bankAccountNumber: String,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendor/order/estimate/pdf?bank_number=$bankAccountNumber&order_id=$orderId")
        )
    }

    suspend fun getPdf(
        orderId: String
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendor/order/pdf?orderId=$orderId")
        )
    }

    suspend fun getProfile(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendor/me")
        )
    }

    suspend fun updateProfile(
        phone: String? = null,
        groupCustomers: String? = null,
        groupRegions: String? = null,
        name: String? = null,
        minOrderAmount: String? = null,
        bankAccountNumber: String? = null,
        logo: Pair<String?, ByteArray>?,
        banner: Pair<String?, ByteArray>?,
        viberNumber: String? = null,
        about: String? = null
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/vendor/me")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        phone?.let {
                            append(
                                "vendor[b2b_vendor_phone]",
                                it
                            )
                        }
                        viberNumber?.let {
                            append(
                                "vendor[viber_messages_phone_number]",
                                it
                            )
                        }
                        about?.let {
                            append(
                                "vendor[about]",
                                it
                            )
                        }
                        name?.let {
                            append(
                                "vendor[name]",
                                it
                            )
                        }
                        minOrderAmount?.let {
                            append(
                                "vendor[vendor_min_order_amount]",
                                it
                            )
                        }
                        bankAccountNumber?.let {
                            append(
                                "vendor[vendor_bank_account_number]",
                                it
                            )
                        }
                        groupCustomers?.let {
                            append(
                                "vendor[b2b_target_customer_group]",
                                it
                            )
                        }
                        groupRegions?.let {
                            append(
                                "vendor[target_customer_region]",
                                it
                            )
                        }
                        logo?.let {
                            if (it.second.isNotEmpty()) {
                                append("vendor[company_logo]", it.second, Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        ContentType.Application.Pdf.contentType
                                    )
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${it.first}\""
                                    )
                                })
                            }
                        }
                        banner?.let {
                            if (it.second.isNotEmpty()) {
                                append("vendor[company_banner]", it.second, Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        ContentType.Application.Pdf.contentType
                                    )
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${it.first}\""
                                    )
                                })
                            }
                        }
                    }.withLog(),
                    boundary = "WebAppBoundary"
                )
            )
        }
    }

    suspend fun updateDelivery(data: VendorDeliveryServiceData): Result<HttpResponse> =
        runCatching {
            return@runCatching HttpClientProvider.client.post(
                url("mobile/vendor/order/shippingDetails")
            ) {
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
                        }.withLog(),
                        "WebAppBoundary"
                    )
                )
            }
        }

    suspend fun getProducts(
        pageSize: Int = 10,
        currentPage: Int,
        queryParams: List<String> = emptyList(),
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            StringBuilder(url("mobile/vendor/product/list?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage"))
                .apply {
                    queryParams.forEach {
                        append(it)
                    }
                }.toString()
        )
    }

    suspend fun getProduct(
        id: String
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendor/product?productId=$id")
        )
    }

    suspend fun saveProduct(
        product: VendorProduct,
        newImages: List<String> = emptyList(),
        removeImages: List<MediaGallery> = emptyList(),
        primaryImage: String? = null,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/vendor/product/save${product.productId?.let { "?id=${product.productId}" } ?: ""}")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("product[name]", product.name ?: "")
                        append("product[status]", product.status ?: "2")
                        append("product[price]", product.price ?: "")
                        product
                            .takeIf { it.specialPrice != null }
                            ?.takeIf { it.specialPriceTo != null }
                            ?.takeIf { it.specialPriceFrom != null }
                            ?.let {
                                append("product[special_price]", product.specialPrice ?: "")
                                append(
                                    "product[special_from_date]",
                                    product.specialPriceFrom ?: ""
                                )
                                append("product[special_to_date]", product.specialPriceTo ?: "")
                            }
                        append("product[short_description]", product.shortDescription ?: "")
                        append("product[description]", product.description ?: "")
                        append("product[b2b_min_qty]", product.minQty ?: "")
                        append("product[min_quantity_unit]", product.minQtyUnit ?: "kom")

                        append("product[stock_data][manage_stock]", product.manageStock ?: "0")
                        if (product.manageStock == "1") {
                            append(
                                "product[quantity_and_stock_status][qty]",
                                product.qty?.ifEmpty { "99999" } ?: "99999")
                        }
                        append(
                            "product[stock_data][use_config_manage_stock]",
                            product.useConfigManageStock ?: "1"
                        )
                        // append(
                        //     "product[quantity_and_stock_status][is_in_stock]",
                        //     product.isInStock ?: ""
                        // )
                        product.categories.forEachIndexed { index, s ->
                            append("product[category_ids][$index]", s)
                        }

                        product
                            .takeIf { it.newsFrom != null }
                            ?.takeIf { it.newsTo != null }
                            ?.let {
                                append("product[news_from_date]", product.newsFrom ?: "")
                                append("product[news_to_date]", product.newsTo ?: "")
                            }
                        append("product[is_karika_exclusive]", product.isExclusive ?: "")
                        append("product[enable_messaging]", product.enabledMessages ?: "")

                        append("product[website_ids][3]", "3")
                        append("product[meta_keyword]", product.name ?: "")
                        append("product[meta_description]", product.name ?: "")
                        append("product[meta_title]", product.name ?: "")

                        newImages.forEachIndexed { index, it ->
                            append(
                                "product[media_gallery][images][${it.hashCode()}][position]",
                                product.mediaGallery.size + 1 + index
                            )
                            append(
                                "product[media_gallery][images][${it.hashCode()}][media_type]",
                                "image"
                            )
                            append("product[media_gallery][images][${it.hashCode()}][file]", it)

                            if (product.mediaGallery.isEmpty()) {
                                append("product[image]", it)
                                append("product[thumbnail]", it)
                                append("product[small_image]", it)
                                append("product[swatch_image]", it)
                            }
                        }
                        primaryImage?.let {
                            append("product[image]", it)
                            append("product[thumbnail]", it)
                            append("product[small_image]", it)
                            append("product[swatch_image]", it)
                        }
                        removeImages.forEach {
                            append(
                                "product[media_gallery][images][${it.id}][removed]",
                                "1"
                            )
                            append(
                                "product[media_gallery][images][${it.id}][file]",
                                it.url ?: ""
                            )
                            append(
                                "product[media_gallery][images][${it.id}][value_id]",
                                it.id ?: ""
                            )
                        }
                    }.withLog(),
                    "WebAppBoundary"
                )
            )
        }
    }

    suspend fun saveProductImage(
        attachment: ByteArray? = null,
        filename: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/vendor/product/image/save")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        attachment?.let {
                            append("image", it, Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    "image/png"
                                )
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"$filename\""
                                )
                            })
                        }
                    }.withLog(),
                    "WebAppBoundary"
                )
            )
        }
    }

    suspend fun productData(
        name: String,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            urlV1("ai/suggestions?query=$name")
        )
    }

    suspend fun notifications(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendor/notifications")
        )
    }

    suspend fun updateOrder(
        orderId: String,
        items: List<Triple<String, String, String>>
    ): Result<HttpResponse> =
        runCatching {
            return@runCatching HttpClientProvider.client.post(
                url("mobile/vendor/order/items/change")
            ) {
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
                                                    "discount" to JsonPrimitive(it.third),
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

    suspend fun markAsRead(
        entityId: String,
    ): Result<HttpResponse> =
        runCatching {
            return@runCatching HttpClientProvider.client.post(
                url("mobile/vendor/notification/mark_read?notificationId=$entityId")
            )
        }

    suspend fun getBytesFromImage(url: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(url)
    }
}

class DashRepository internal constructor() {
    fun get(
    ): Flow<ResultState<DashboardData>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().get().getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<DashboardData>()))
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
        pageSize: Int = 10,
        currentPage: Int,
        queryParams: List<String> = emptyList()
    ): Flow<ResultState<List<VendorOrder>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getOrders(pageSize, currentPage, queryParams).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<VendorOrder>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrder(id: String): Flow<ResultState<VendorOrder>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getOrder(id).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorOrder>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getOrderComments(id: String): Flow<ResultState<List<Comment>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getOrderComments(id).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Comment>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun sendComment(
        orderId: String,
        comment: String,
        attachment: ByteArray? = null,
        filename: String = ""
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response =
                DashApi().sendComment(orderId, comment, attachment, filename).getOrNoInternet()

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

    fun changeOrderStatus(
        type: String,
        orderId: String,
        message: String,
        withDelivery: Boolean? = null,
        attachment: ByteArray? = null,
        filename: String? = null
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().changeOrderStatus(
                type, orderId, message, withDelivery, attachment, filename
            ).getOrNoInternet()

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

    fun createInvoice(
        orderId: String,
        bankAccountNumber: String
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().createInvoice(orderId, bankAccountNumber).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getPdf(
        orderId: String
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getPdf(orderId).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateProfile(
        phone: String? = null,
        groupCustomers: String? = null,
        groupRegions: String? = null,
        name: String? = null,
        minOrderAmount: String? = null,
        bankAccountNumber: String? = null,
        logo: Pair<String?, ByteArray>? = null,
        banner: Pair<String?, ByteArray>? = null,
        viberNumber: String? = null,
        about: String? = null
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().updateProfile(
                phone,
                groupCustomers,
                groupRegions,
                name,
                minOrderAmount,
                bankAccountNumber,
                logo,
                banner,
                viberNumber,
                about
            ).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getProfile(): Flow<ResultState<Vendor>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getProfile().getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Vendor>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateDelivery(data: VendorDeliveryServiceData): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().updateDelivery(data).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>()))
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
        pageSize: Int = 10,
        currentPage: Int,
        queryParams: List<String> = emptyList()
    ): Flow<ResultState<List<VendorProduct>>> = flow {
        emit(ResultState.Loading)
        try {
            val response =
                DashApi().getProducts(pageSize, currentPage, queryParams).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<VendorProduct>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getProduct(id: String): Flow<ResultState<VendorProduct>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().getProduct(id).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorProduct>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun saveProduct(
        product: VendorProduct,
        newImages: List<String> = emptyList(),
        removeImages: List<MediaGallery> = emptyList(),
        primaryImage: String? = null,
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().saveProduct(product, newImages, removeImages, primaryImage)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun saveProductImage(
        attachment: ByteArray? = null,
        filename: String = "",
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().saveProductImage(attachment, filename)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun productData(
        name: String
    ): Flow<ResultState<AIResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().productData(name)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<AIResponse>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun notifications(): Flow<ResultState<List<Notification>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = DashApi().notifications()
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Notification>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateOrder(
        orderId: String,
        items: List<Triple<String, String, String>>
    ): Flow<ResultState<String>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = DashApi().updateOrder(orderId, items)
                    .getOrNoInternet()

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

    fun markAsRead(
        id: String,
    ): Flow<ResultState<String>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = DashApi().markAsRead(id)
                    .getOrNoInternet()

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

    fun getBytesFromImage(
        url: String,
    ): Flow<ResultState<ByteArray>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = DashApi().getBytesFromImage(url)
                    .getOrNoInternet()

                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.bodyAsBytes()))
                    return@flow
                }

                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

}