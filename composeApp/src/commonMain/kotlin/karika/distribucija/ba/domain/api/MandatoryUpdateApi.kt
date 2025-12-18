package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.MandatoryUpdate
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class MandatoryUpdateApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/versions")
        )
    }
}

class MandatoryUpdateRepository internal constructor() {
    fun get(): Flow<ResultState<MandatoryUpdate>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MandatoryUpdateApi()
                .get()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<MandatoryUpdate>()))
            } else {
                emit(
                    ResultState.Error("")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}