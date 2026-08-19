package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.domain.model.ChangePasswordRequest
import karika.distribucija.ba.domain.model.ChangePasswordResponse
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.ForgotPasswordRequest
import karika.distribucija.ba.domain.model.NotificationPreferences
import karika.distribucija.ba.domain.model.NotificationPreferencesResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.domain.model.UpdateNotificationPreferencesRequest
import karika.distribucija.ba.domain.model.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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

    suspend fun deleteAccount(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/customer/me/delete")
        )
    }

    suspend fun notificationPreferences(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/customer/notification-preferences")
        )
    }

    suspend fun updateNotificationPreferences(
        request: UpdateNotificationPreferencesRequest
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.put(
            url("mobile/customer/notification-preferences")
        ) {
            setBody(request)
        }
    }
}

class UserRepository internal constructor() {
    fun get(): Flow<ResultState<UserDetails>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .get()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<UserDetails>()))
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

    fun put(userDetails: UpdateCustomerRequest): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .put(userDetails)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
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

    fun blogs(): Flow<ResultState<List<Blog>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .blogs()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Blog>>() ))
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

    fun blog(id: String): Flow<ResultState<Blog>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .blog(id)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Blog>() ))
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

    fun config(): Flow<ResultState<Config>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .config()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Config>() ))
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

    fun forgotPass(email: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .forgotPass(email)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(
                    ResultState.Success(
                        "Ako postoji nalog povezan sa '$email', dobićete e-poruku sa vezom za resetovanje Vaše lozinke."
                    )
                )
            } else {
                emit(
                    ResultState.Error("Ako postoji nalog povezan sa '$email', dobićete e-poruku sa vezom za resetovanje Vaše lozinke.")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun changePass(old: String, new: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .change(old, new)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val body = response.body<ChangePasswordResponse>()
                if (body.message == "The old password is incorrect.") {
                    emit(ResultState.Success("Stara lozinka je pogrešna!"))
                    return@flow
                }

                if (body.status == "true") {
                    emit(ResultState.Success("Lozinka uspješno promijenjena!"))
                    return@flow
                }
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )

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

    fun deleteAccount(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .deleteAccount()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>() ))
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

    fun getNotificationPreferences(): Flow<ResultState<NotificationPreferences>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .notificationPreferences()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<NotificationPreferencesResponse>().notificationPreferences))
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

    fun updateNotificationPreferences(
        preferences: NotificationPreferences
    ): Flow<ResultState<NotificationPreferences>> = flow {
        emit(ResultState.Loading)
        try {
            val response = UserApi()
                .updateNotificationPreferences(UpdateNotificationPreferencesRequest(preferences))
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<NotificationPreferencesResponse>().notificationPreferences))
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