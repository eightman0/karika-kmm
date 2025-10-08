package karika.distribucija.ba.domain.api

import androidx.compose.foundation.content.MediaType
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.domain.model.ChangePasswordRequest
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.ForgotPasswordRequest
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.domain.model.UserDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class UserApi {
    suspend fun get(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("customers/me")
        )
    }

    suspend fun put(userDetails: UpdateCustomerRequest): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.put(
            url("customers/me")
        ) {
            setBody(userDetails)
        }
    }

    suspend fun blogs(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("blogs/list")
        )
    }

    suspend fun blog(id: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("blogs/one/$id")
        )
    }

    suspend fun config(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/config")
        )
    }

    suspend fun forgotPass(email: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            urlV1("auth/reset-password")
        ) {
            setBody(
                ForgotPasswordRequest(
                    email = email,
                    template = "email_reset"
                )
            )
        }
    }

    suspend fun change(old: String, new: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            urlV1("auth/change-password")
        ) {
            header(HttpHeaders.Accept, "application/json")
            setBody(ChangePasswordRequest(old, new, new))
        }
    }
}

class UserRepository internal constructor() {
    fun get(): Flow<ResultState<UserDetails>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .get()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun put(userDetails: UpdateCustomerRequest): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .put(userDetails)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun blogs(): Flow<ResultState<List<Blog>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .blogs()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun blog(id: String): Flow<ResultState<Blog>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .blog(id)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun config(): Flow<ResultState<Config>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .config()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun forgotPass(email: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .forgotPass(email)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(
                    ResultState.Success(
                        "Ako postoji nalog povezan sa '$email', dobićete e-poruku sa vezom za resetovanje vaše lozinke."
                    )
                )
            } else {
                emit(
                    ResultState.Error("Ako postoji nalog povezan sa '$email', dobićete e-poruku sa vezom za resetovanje vaše lozinke.")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun changePass(old: String, new: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .change(old, new)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }
}