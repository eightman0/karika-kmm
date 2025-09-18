package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class VendorApi {
    suspend fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
        sortType: String = "ASC"
    ): Result<HttpResponse> = runCatching {
        val url =
            when {
                filterValue.isNotEmpty() ->
                    url("mobile/vendors?" +
                            (filterValue.takeIf { it.isNotEmpty() }?.let {
                                "searchCriteria[filterGroups][0][filters][0][field]=region_uid" +
                                        "&searchCriteria[filterGroups][0][filters][0][value]=$it" +
                                        "&searchCriteria[filterGroups][0][filters][0][conditionType]=in"
                            } ?: "") +

                            "&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                searchText.isNotEmpty() ->
                    url("mobile/vendors?searchCriteria[filterGroups][0][filters][0][field]=public_name&searchCriteria[filterGroups][0][filters][0][value]=$searchText&searchCriteria[filterGroups][0][filters][0][conditionType]=like&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                else ->
                    url("mobile/vendors?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")
            }

        return@runCatching HttpClientProvider.client.get(url)
    }
}

class VendorRepository internal constructor() {
    fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
        sortType: String = "ASC"
    ): Flow<ResultState<List<Vendor>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = VendorApi().vendors(
                searchText,
                pageSize,
                currentPage,
                filterBy,
                filterValue,
                sortType
            ).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }
}