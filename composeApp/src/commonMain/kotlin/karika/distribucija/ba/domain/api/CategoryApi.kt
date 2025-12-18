package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

internal class CategoryApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("categories")
        )
    }
}

class CategoryRepository internal constructor() {
    fun get(): Flow<ResultState<Category>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CategoryApi()
                .get()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Category>()))
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