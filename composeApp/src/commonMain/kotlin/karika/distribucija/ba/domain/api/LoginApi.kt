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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
            } else {
                emit(
                    ResultState.Error(
                        if (response.bodyAsText().contains("Failure(java.net.UnknownHostException:")
                        ) {
                            "Nema internet konekcije, provjerite Vašu vezu i pokušajte ponovo."
                        } else if (response.bodyAsText()
                                .contains("You're not allowed to login here")
                        ) {
                            if (loginDto.userType.isShop())
                                "Ovaj ${loginDto.username} račun je napravljen samo za dobavljača, prijavite se kao dobavljač"
                            else "Ovaj ${loginDto.username} račun je napravljen samo za kupca, prijavite se kao kupac"
                        } else {
                            "Prijava na račun je bila pogrešna ili je Vaš račun privremeno onemogućen. Molimo pričekajte i pokušajte ponovo kasnije."
                        }
                    )
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}

fun Result<HttpResponse>.getOrNoInternet(): HttpResponse {
    return this.getOrElse {
        if (it.message?.contains("Unable to resolve host") == true) {
            throw Exception("Nema internet konekcije, provjerite Vašu vezu i pokušajte ponovo.")
        }
        throw Exception("Došlo je do greške. Pokušajte ponovo!")
    }
}