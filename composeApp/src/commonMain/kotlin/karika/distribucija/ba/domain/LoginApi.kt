package karika.distribucija.ba.domain

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class LoginApi {
    suspend fun login(loginDto: LoginDto): Result<String> = runCatching {
        return@runCatching HttpClientProvider.client.post("https://test.karika.ba/magento/rest/V1/integration/customer/token?type=customer") {
            setBody(loginDto)
        }.body()
    }
}

class LoginRepository internal constructor() {
    fun login(loginDto: LoginDto): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val data = LoginApi().login(loginDto)
            HttpClientProvider.token = data.getOrNull()?.replace("\"", "")
            emit(ResultState.Success(HttpClientProvider.token ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }
}