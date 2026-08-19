package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.PartnershipErrorResponse
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.PartnershipRequestsResponse
import karika.distribucija.ba.domain.model.RejectPartnershipAction
import karika.distribucija.ba.domain.model.RejectPartnershipRequest
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.partnershipErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class PartnershipApi {
    suspend fun list(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("vendor-operations/me/partnership-requests")
        )
    }

    suspend fun approve(partnershipId: Long): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("vendor-operations/partnerships/$partnershipId/approve")
        )
    }

    suspend fun reject(partnershipId: Long, reason: String?): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("vendor-operations/partnerships/$partnershipId/reject")
        ) {
            val trimmedReason = reason?.trim()
            if (!trimmedReason.isNullOrEmpty()) {
                setBody(RejectPartnershipRequest(RejectPartnershipAction(trimmedReason)))
            }
        }
    }
}

class PartnershipRepository internal constructor() {
    fun list(): Flow<ResultState<List<PartnershipRequest>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = PartnershipApi()
                .list()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<PartnershipRequestsResponse>().items))
            } else {
                emit(ResultState.Error(response.errorMessage()))
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun approve(partnershipId: Long): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = PartnershipApi()
                .approve(partnershipId)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
            } else {
                emit(ResultState.Error(response.errorMessage()))
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun reject(partnershipId: Long, reason: String?): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = PartnershipApi()
                .reject(partnershipId, reason)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
            } else {
                emit(ResultState.Error(response.errorMessage()))
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}

private suspend fun HttpResponse.errorMessage(): String {
    return try {
        partnershipErrorMessage(body<PartnershipErrorResponse>().parameters?.code)
    } catch (e: Exception) {
        "Došlo je do greške. Pokušajte ponovo!"
    }
}
