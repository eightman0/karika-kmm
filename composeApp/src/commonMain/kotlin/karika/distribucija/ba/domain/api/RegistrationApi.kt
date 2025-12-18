package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.ConfirmRegistration
import karika.distribucija.ba.domain.model.ErrorResponse
import karika.distribucija.ba.domain.model.RegisterDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorRegisterRequest
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

    suspend fun registerVendor(data: VendorRegisterRequest): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/vendor/register")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("vendor[b2b_vendor_pravno_lice]", data.companyName)
                        append("vendor[b2b_vendor_entitet]", data.entity)
                        append("vendor[b2b_vendor_kanton]", data.canton)
                        append("vendor[b2b_vendor_grad]", data.city)
                        append("vendor[b2b_vendor_pdv_broj]", data.pdvNumber)
                        append("vendor[b2b_vendor_id]", data.idNumber)
                        append("vendor[b2b_target_customer_group]", data.customerGroup)
                        append("vendor[target_customer_region]", data.customersRegion)
                        append("vendor[b2b_vendor_phone]", data.phone)
                        append("email", data.email)
                        append("password", data.pass)
                        append("password_confirmation", data.repeatPass)
                        append("firstname", data.firstname)
                        append("lastname", data.lastname)
                    }.withLog(),
                    "WebAppBoundary"
                )
            )
        }
    }

    suspend fun confirmRegistration(data: ConfirmRegistration): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            urlV1("auth/register")
        ) {
            setBody(data)
        }
    }
}

class RegistrationRepository internal constructor() {
    fun register(registerDto: RegisterDto): Flow<ResultState<RegisterDto>> = flow {
        emit(ResultState.Loading)
        try {
            val response = RegistrationApi()
                .register(registerDto)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            val errorMsg = when (response.body<ErrorResponse>().message) {
                "A customer with the same email address already exists in an associated website." -> "Ovaj email je već u upotrebi."
                "E-mail adresa nije u ispravnom formatu." -> "E-mail adresa nije u ispravnom formatu."
                else -> "Došlo je do greške. Pokušajte ponovo!"
            }
            emit(ResultState.Error(errorMsg))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun registerVendor(registerDto: VendorRegisterRequest): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = RegistrationApi()
                .registerVendor(registerDto)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText()))
                return@flow
            }

            emit(
                ResultState.Error(
                    response.body<ErrorResponse>().message
                )
            )
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun confirmRegister(confirmRegistration: ConfirmRegistration): Flow<ResultState<String>> =
        flow {
            emit(ResultState.Loading)
            try {
                RegistrationApi()
                    .confirmRegistration(confirmRegistration)
                    .getOrNoInternet()
                emit(ResultState.Success("Uspješno registrovano"))
            } catch (e: Exception) {
                emit(ResultState.Error(e.message))
            }
        }
}