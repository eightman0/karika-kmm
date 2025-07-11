package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class OrdersApi {
    suspend fun orders(
        pageSize: Int = 10,
        currentPage: Int = 1,
        filterValue: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/orders?pageSize=$pageSize&currentPage=$currentPage&status=$filterValue")
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
    fun vendors(
        pageSize: Int = 10,
        currentPage: Int = 1,
        filterValue: String = ""
    ): Flow<ResultState<List<OrdersResponse>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = OrdersApi()
                .orders(
                    pageSize,
                    currentPage,
                    filterValue
                ).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

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
                ).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

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
                ).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

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
                ).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }
}