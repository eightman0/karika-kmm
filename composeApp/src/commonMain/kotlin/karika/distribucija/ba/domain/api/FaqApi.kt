package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FaqApi {
    suspend fun faq(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/faq")
        )
    }

}

class FaqRepository internal constructor() {
    fun faq(): Flow<ResultState<List<Faq>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = FaqApi()
                .faq()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        }
    }
}