package karika.distribucija.ba.domain.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.KarikaTracking
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class AnalyticsApi {
    suspend fun post(tracking: KarikaTracking): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            urlV1("tracking")
        ) {
            setBody(tracking)
        }
    }
}

class AnalyticsRepository internal constructor() {
    fun post(tracking: KarikaTracking): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = AnalyticsApi()
                .post(tracking)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}