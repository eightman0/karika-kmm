package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.EmployeeLocationHistory
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorEmployeeSearchResults
import karika.distribucija.ba.domain.model.VendorOperationsError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class EmployeesApi {

    /** GET /V1/vendor-operations/employees - call only when `/me` has `can_view_employees`. */
    suspend fun getEmployees(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/employees")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            parameter("searchCriteria[sort_orders][0][field]", "created_at")
            parameter("searchCriteria[sort_orders][0][direction]", "DESC")

            var groupIdx = 0

            if (!search.isNullOrBlank()) {
                // OR: email / firstname / lastname like %search%
                listOf("email", "firstname", "lastname").forEachIndexed { i, field ->
                    parameter("searchCriteria[filter_groups][$groupIdx][filters][$i][field]", field)
                    parameter("searchCriteria[filter_groups][$groupIdx][filters][$i][value]", "%$search%")
                    parameter("searchCriteria[filter_groups][$groupIdx][filters][$i][condition_type]", "like")
                }
                groupIdx++
            }

            if (status != null) {
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][field]", "status")
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][value]", status)
                parameter("searchCriteria[filter_groups][$groupIdx][filters][0][condition_type]", "eq")
            }
        }
    }

    /** GET /V1/vendor-operations/employees/{employeeId}/locations - dates are inclusive `YYYY-MM-DD`. */
    suspend fun getLocations(
        employeeId: Long,
        dateFrom: String,
        dateTo: String
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("vendor-operations/employees/$employeeId/locations")) {
            parameter("dateFrom", dateFrom)
            parameter("dateTo", dateTo)
        }
    }
}

class EmployeesRepository internal constructor() {

    private val api = EmployeesApi()

    fun getEmployees(
        page: Int,
        pageSize: Int,
        search: String? = null,
        status: String? = null
    ): Flow<ResultState<VendorEmployeeSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getEmployees(page, pageSize, search, status).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<VendorEmployeeSearchResults>()))
                return@flow
            }
            emit(ResultState.Error(response.errorMessage()))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getLocations(
        employeeId: Long,
        dateFrom: String,
        dateTo: String
    ): Flow<ResultState<EmployeeLocationHistory>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getLocations(employeeId, dateFrom, dateTo).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<EmployeeLocationHistory>()))
                return@flow
            }
            emit(ResultState.Error(response.errorMessage()))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    /** 403/404/400 here are permission or validation results, never a logged-out session. */
    private suspend fun HttpResponse.errorMessage(): String {
        val error = runCatching { body<VendorOperationsError>() }.getOrNull()
        return when (error?.code) {
            "feature_disabled_for_vendor" -> "Vendor Operations nije uključen za ovog distributera."
            "forbidden" -> "Nemate pravo pregleda ovih podataka."
            "employee_not_found" -> "Komercijalista nije pronađen."
            "validation_error" -> error.message ?: "Neispravan period."
            else -> error?.message ?: "Došlo je do greške. Pokušajte ponovo!"
        }
    }
}
