package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class OrdersApi {
    suspend fun orders(
        pageSize: Int = 30,
        currentPage: Int = 1,
        filterValue: String = "",
        sortBy: String = "",
        sortDirection: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/orders?pageSize=$pageSize&currentPage=$currentPage&status=$filterValue&sortBy=$sortBy&sortDirection=$sortDirection")
        )
    }

    suspend fun order(
        orderId: String,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/order?orderId=$orderId")
        )
    }

    suspend fun comments(
        orderId: String?,
        vendorId: String?,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/orders/message?orderId=$orderId&vendorId=$vendorId")
        )
    }

    suspend fun sendComment(
        orderId: String?,
        vendorId: String?,
        comment: String?,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/orders/message?orderId=$orderId&vendorId=$vendorId&message=$comment")
        )
    }

    suspend fun sendBill(
        orderId: String,
        vendorId: String,
        comment: String?,
        attachment: ByteArray?,
        filename: String,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/orders/add_bill")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("orderId", orderId)
                        append("vendorId", vendorId)
                        append("message", comment ?: "")
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

    suspend fun cancel(
        orderId: String?,
        vendorId: String?,
        reason: String?,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/orders/cancel?orderId=$orderId&vendorId=$vendorId&message=$reason")
        )
    }
}

class OrdersRepository internal constructor() {
    fun orders(
        pageSize: Int = 30,
        currentPage: Int = 1,
        filterValue: String = "",
        sortBy: String = "",
        sortDirection: String = "",
    ): Flow<ResultState<List<OrdersResponse>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .orders(
                    pageSize,
                    currentPage,
                    filterValue,
                    sortBy,
                    sortDirection
                ).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<OrdersResponse>>() ))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun order(
        orderId: String = "",
    ): Flow<ResultState<OrdersResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .order(orderId).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<OrdersResponse>() ))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun comments(
        orderId: String?,
        vendorId: String?,
    ): Flow<ResultState<List<Comment>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .comments(
                    orderId,
                    vendorId
                ).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Comment>>() ))
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
        orderId: String?,
        vendorId: String?,
        comment: String?,
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .sendComment(
                    orderId,
                    vendorId,
                    comment
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

    fun sendBill(
        orderId: String,
        vendorId: String,
        comment: String?,
        attachment: ByteArray?,
        filename: String,
    ): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .sendBill(
                    orderId,
                    vendorId,
                    comment,
                    attachment,
                    filename
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

    fun cancel(
        orderId: String?,
        vendorId: String?,
        reason: String?,
    ): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .cancel(
                    orderId,
                    vendorId,
                    reason
                ).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
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