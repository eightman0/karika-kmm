package karika.distribucija.ba.domain.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.RegisterDto
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class RegistrationApi {
    suspend fun register(registerDto: RegisterDto): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("customers")
        ) {
            setBody(registerDto)
        }
    }
}

class RegistrationRepository internal constructor() {
    fun register(registerDto: RegisterDto): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = RegistrationApi()
                .register(registerDto)
                .getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }
}