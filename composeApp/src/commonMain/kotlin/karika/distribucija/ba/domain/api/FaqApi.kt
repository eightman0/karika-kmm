package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class FaqApi {
    suspend fun faq(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            urlV1("static/faq")
        )
    }

}

class FaqRepository internal constructor() {
    fun faq(): Flow<ResultState<List<Faq>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = FaqApi()
                .faq()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Faq>>() ))
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