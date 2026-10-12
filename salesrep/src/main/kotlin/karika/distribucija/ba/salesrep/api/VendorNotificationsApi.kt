package karika.distribucija.ba.salesrep.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.model.VendorNotificationSearchResults
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** `/V1/vendor-operations/notifications` (Chat V2 mobile spec) - mirrors the vendor notification
 * calls in composeApp's DashApi.kt / DashRepository, which replaced the legacy
 * `mobile/vendor/notifications` feed. */
class VendorNotificationsApi {

    /** [isRead] filters via a Magento `filter_groups` clause on `is_read` (null = all). */
    suspend fun getNotifications(page: Int, pageSize: Int, isRead: Boolean? = null): Result<HttpResponse> = runCatching {
        val filterParams = if (isRead != null) {
            "&searchCriteria[filter_groups][0][filters][0][field]=is_read" +
                "&searchCriteria[filter_groups][0][filters][0][value]=${if (isRead) 1 else 0}" +
                "&searchCriteria[filter_groups][0][filters][0][condition_type]=eq"
        } else ""
        HttpClientProvider.client.get(
            HttpClientProvider.url("vendor-operations/notifications?searchCriteria[current_page]=$page&searchCriteria[page_size]=$pageSize$filterParams")
        )
    }

    suspend fun markRead(notificationId: String): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/notifications/$notificationId/mark-read"))
    }

    suspend fun markAllRead(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/notifications/mark-all-read"))
    }
}

class VendorNotificationsRepository {

    fun notifications(
        page: Int = 1,
        pageSize: Int = 50,
        isRead: Boolean? = null
    ): Flow<ResultState<VendorNotificationSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = VendorNotificationsApi().getNotifications(page, pageSize, isRead).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorNotificationSearchResults>()))
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

    fun markRead(notificationId: String): Flow<ResultState<Unit>> = simpleCall { VendorNotificationsApi().markRead(notificationId) }

    fun markAllRead(): Flow<ResultState<Unit>> = simpleCall { VendorNotificationsApi().markAllRead() }

    private fun simpleCall(call: suspend () -> Result<HttpResponse>): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = call().getOrNoInternet()
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

    private companion object {
        const val TAG = "VendorNotifications"
    }
}
