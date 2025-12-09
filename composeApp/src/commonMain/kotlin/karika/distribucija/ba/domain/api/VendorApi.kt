package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class VendorApi {
    @Suppress("UNUSED_PARAMETER")
    suspend fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
        sortType: String = "ASC"
    ): Result<HttpResponse> = runCatching {
        val url = when {
            filterValue.isNotEmpty() && searchText.isNotEmpty() -> {
                val groups = mutableListOf<String>()
                var idx = 0
                val firstField = filterBy.ifEmpty { "region_uid" }
                groups.add(
                    "searchCriteria[filterGroups][${idx}][filters][0][field]=$firstField" +
                            "&searchCriteria[filterGroups][${idx}][filters][0][value]=$filterValue" +
                            "&searchCriteria[filterGroups][${idx}][filters][0][conditionType]=in"
                )
                idx++
                groups.add(
                    "searchCriteria[filterGroups][${idx}][filters][0][field]=public_name" +
                            "&searchCriteria[filterGroups][${idx}][filters][0][value]=$searchText" +
                            "&searchCriteria[filterGroups][${idx}][filters][0][conditionType]=like"
                )
                url(
                    "mobile/vendors?" +
                            groups.joinToString(separator = "&") +
                            "&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage"
                )
            }

            filterValue.isNotEmpty() ->
                url("mobile/vendors?" +
                        (filterValue.takeIf { it.isNotEmpty() }?.let {
                            val field = filterBy.ifEmpty { "region_uid" }
                            "searchCriteria[filterGroups][0][filters][0][field]=$field" +
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
            ).getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }
}