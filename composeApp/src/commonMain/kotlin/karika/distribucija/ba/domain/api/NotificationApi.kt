package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorNotificationSearchResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

internal class NotificationApi {
    suspend fun save(pushHandle: String?, tokenId: String?): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/push/token?token=$pushHandle&tokenId=${tokenId}")
        )
    }

    /** `/V1/vendor-operations/notifications` (Chat V2 mobile spec) - replaces `get()` above.
     * Per spec, plain customers skip the vendor-only feature guard on this route. [isRead]
     * filters via a Magento `filter_groups` clause on `is_read` (null = no filter). */
    suspend fun getVendorNotifications(
        page: Int,
        pageSize: Int,
        isRead: Boolean? = null
    ): Result<HttpResponse> = runCatching {
        val filterParams = if (isRead != null) {
            "&searchCriteria[filter_groups][0][filters][0][field]=is_read" +
                "&searchCriteria[filter_groups][0][filters][0][value]=${if (isRead) 1 else 0}" +
                "&searchCriteria[filter_groups][0][filters][0][condition_type]=eq"
        } else ""
        return@runCatching HttpClientProvider.client.get(
            url("vendor-operations/notifications?searchCriteria[current_page]=$page&searchCriteria[page_size]=$pageSize$filterParams")
        )
    }

    suspend fun markVendorNotificationRead(notificationId: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("vendor-operations/notifications/$notificationId/mark-read")
        )
    }

    suspend fun markAllVendorNotificationsRead(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("vendor-operations/notifications/mark-all-read")
        )
    }
}

class NotificationRepository internal constructor() {
    fun vendorNotifications(
        page: Int = 1,
        pageSize: Int = 50,
        isRead: Boolean? = null
    ): Flow<ResultState<VendorNotificationSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .getVendorNotifications(page, pageSize, isRead)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorNotificationSearchResults>()))
            } else {
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markVendorNotificationRead(notificationId: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .markVendorNotificationRead(notificationId)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
            } else {
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markAllVendorNotificationsRead(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .markAllVendorNotificationsRead()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
            } else {
                emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun savePushHandle(pushHandle: String?, tokenId: String?): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .save(pushHandle, tokenId)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}