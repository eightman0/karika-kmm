package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Bonus
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class PointsApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/reward/points")
        )
    }

    suspend fun trx(
        pageSize: Int = 30,
        currentPage: Int = 1
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/reward/transactions?pageSize=$pageSize&currentPage=$currentPage")
        )
    }

}

class PointsRepository internal constructor() {
    fun get(): Flow<ResultState<Bonus>> = flow {
        emit(ResultState.Loading)
        try {
            val response = PointsApi()
                .get()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Bonus>() ))
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

    fun trx(
        pageSize: Int = 30,
        currentPage: Int = 1
    ): Flow<ResultState<List<Transaction>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = PointsApi()
                .trx(pageSize, currentPage)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Transaction>>() ))
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