package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ErrorResponse
import karika.distribucija.ba.domain.model.EmployeeLocationSubmit
import karika.distribucija.ba.domain.model.EmployeeLocationSubmitBody
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Stable, app-generated device id persisted via [PersistenceManager] - not an IMEI, never used for auth. */
object DeviceIdentifier : KoinComponent {
    private val persistenceManager: PersistenceManager = get()

    @OptIn(ExperimentalUuidApi::class)
    fun deviceId(): String {
        val existing = persistenceManager.get("device_id")
        if (existing.isNotEmpty()) return existing
        val generated = Uuid.random().toString()
        persistenceManager.save("device_id", generated)
        return generated
    }
}

internal class EmployeeLocationApi {
    /** POST /V1/vendor-operations/employee/location */
    suspend fun submit(body: EmployeeLocationSubmitBody): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("vendor-operations/employee/location")) {
            setBody(body)
        }
    }
}

class EmployeeLocationRepository internal constructor() {

    private val api = EmployeeLocationApi()

    fun submit(location: EmployeeLocationSubmit): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.submit(EmployeeLocationSubmitBody(location)).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
