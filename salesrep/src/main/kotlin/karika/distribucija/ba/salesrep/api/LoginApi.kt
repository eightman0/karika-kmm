package karika.distribucija.ba.salesrep.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.salesrep.model.LoginRequest
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class LoginApi {
    suspend fun login(request: LoginRequest): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("integration/customer/token?type=vendor")) {
            setBody(request)
        }
    }
}

class LoginRepository internal constructor() {
    fun login(username: String, password: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = LoginApi().login(LoginRequest(username, password)).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText().replace("\"", "")))
                return@flow
            }
            emit(
                ResultState.Error(
                    if (response.bodyAsText().contains("Failure(java.net.UnknownHostException:")) {
                        "Nema internet konekcije, provjerite Vašu vezu i pokušajte ponovo."
                    } else {
                        "Prijava na račun je bila pogrešna ili je Vaš račun privremeno onemogućen. Molimo pričekajte i pokušajte ponovo kasnije."
                    }
                )
            )
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}

internal fun Result<HttpResponse>.getOrNoInternet(): HttpResponse = getOrElse {
    if (it.message?.contains("Unable to resolve host") == true) {
        throw Exception("Nema internet konekcije, provjerite Vašu vezu i pokušajte ponovo.")
    }
    throw Exception("Došlo je do greške. Pokušajte ponovo!")
}
