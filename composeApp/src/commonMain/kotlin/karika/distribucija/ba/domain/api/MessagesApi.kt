package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Shop
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class MessagesApi {
    suspend fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            when {
                filterBy.isNotEmpty() ->
                    url("mobile/vendors?searchCriteria[filterGroups][0][filters][0][field]=$filterBy&searchCriteria[filterGroups][0][filters][0][value]=$filterValue&searchCriteria[filterGroups][0][filters][0][conditionType]=equals&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                searchText.isNotEmpty() ->
                    url("mobile/vendors?searchCriteria[filterGroups][0][filters][0][field]=public_name&searchCriteria[filterGroups][0][filters][0][value]=$searchText&searchCriteria[filterGroups][0][filters][0][conditionType]=like&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                else ->
                    url("mobile/vendors?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")
            }
        )
    }

    suspend fun shops(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            when {
                filterBy.isNotEmpty() ->
                    url("mobile/vendor/customers?searchCriteria[filterGroups][0][filters][0][field]=$filterBy&searchCriteria[filterGroups][0][filters][0][value]=$filterValue&searchCriteria[filterGroups][0][filters][0][conditionType]=equals&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                searchText.isNotEmpty() ->
                    url("mobile/vendor/customers?searchCriteria[filterGroups][0][filters][0][field]=b2b_pravno_lice&searchCriteria[filterGroups][0][filters][0][value]=$searchText&searchCriteria[filterGroups][0][filters][0][conditionType]=like&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                else ->
                    url("mobile/vendor/customers?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")
            }
        )
    }
}

class MessagesRepository internal constructor() {
    fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Flow<ResultState<List<Vendor>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .vendors(searchText, pageSize, currentPage, filterBy, filterValue)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Vendor>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun shops(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Flow<ResultState<List<Shop>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .shops(searchText, pageSize, currentPage, filterBy, filterValue)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<Shop>>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}

fun List<PartData>.withLog(): List<PartData> {
    also {
        val json = buildJsonObject {
            this@withLog.filterIsInstance<PartData.FormItem>()
                .forEach { formItem ->
                    put(formItem.name ?: "", formItem.value)
                }
        }
        println("RequestBody: $json")
    }
    return this
}
