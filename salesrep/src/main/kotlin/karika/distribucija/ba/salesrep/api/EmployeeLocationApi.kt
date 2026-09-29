package karika.distribucija.ba.salesrep.api

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.model.EmployeeLocationSubmit
import karika.distribucija.ba.salesrep.model.EmployeeLocationSubmitBody
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** POST /V1/vendor-operations/employee/location - same Magento endpoint composeApp's
 * EmployeeLocationApi.kt posts to. Only an active vendor_operations_employee may submit. */
internal class EmployeeLocationApi {
    suspend fun submit(location: EmployeeLocationSubmit): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("vendor-operations/employee/location")) {
            setBody(EmployeeLocationSubmitBody(location))
        }
    }
}

class EmployeeLocationRepository internal constructor() {

    companion object {
        private const val TAG = "EmployeeLocationRepository"
    }

    fun submit(location: EmployeeLocationSubmit): Flow<ResultState<Unit>> = flow {
        emit(ResultState.Loading)
        try {
            val response = EmployeeLocationApi().submit(location).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(Unit))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
