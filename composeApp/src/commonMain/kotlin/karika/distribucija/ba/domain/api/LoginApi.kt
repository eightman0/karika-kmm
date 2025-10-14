package karika.distribucija.ba.domain.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class LoginApi {
    suspend fun login(loginDto: LoginDto): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("integration/customer/token?type=${loginDto.getType()}")
        ) {
            setBody(loginDto)
        }
    }
}

class LoginRepository internal constructor() {
    fun login(loginDto: LoginDto): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = LoginApi()
                .login(loginDto)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
            } else {
                emit(
                    ResultState.Error(
                        if (response?.bodyAsText()
                                ?.contains("You're not allowed to login here") == true
                        ) {
                            if (loginDto.userType.isShop())
                                "Ovaj ${loginDto.username} račun je napravljen samo za dobavljača, prijavite se kao dobavljač"
                            else "Ovaj ${loginDto.username} račun je napravljen samo za kupca, prijavite se kao kupac"
                        } else {
                            "Prijava na račun je bila pogrešna ili je vaš račun privremeno onemogućen. Molimo pričekajte i pokušajte ponovo kasnije."
                        }
                    )
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        }
    }
}