package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Notification
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class NotificationApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/customer/notifications")
        )
    }

    suspend fun put(id: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/customer/notification/mark_read?notificationId=$id")
        )
    }

    suspend fun save(pushHandle: String?, tokenId: String?): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/push/token?token=$pushHandle&tokenId=${tokenId}")
        )
    }
}

class NotificationRepository internal constructor() {
    fun get(): Flow<ResultState<List<Notification>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .get()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun put(id: String): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        try {
            val response = NotificationApi()
                .put(id)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

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
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }
}