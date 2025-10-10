package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ProductResponse
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.util.addConditionally
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class ProductApi {
    suspend fun karikaProducts(
        pageSize: Int = 30,
        currentPage: Int,
        searchText: String = "",
        from: String = "",
        to: String = "",
        onlyWithImage: Boolean = false,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url(
                "products?searchCriteria[filterGroups][0][filters][0][field]=is_karika_suggestion&searchCriteria[filterGroups][0][filters][0][value]=1&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage"
            )
        )
    }

    suspend fun promotedVendors(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/vendors/promoted")
        )
    }

    suspend fun search(
        categoryId: String? = null,
        regionId: String? = null,
        vendorId: Int? = null,
        from: String? = null,
        to: String? = null,
        pageSize: Int = 30,
        currentPage: Int = 1,
        searchText: String = "",
        sortBy: String = "price",
        sortType: String = "ASC",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url(
                "" +
                        "mobile/product/search" +
                        "?query=$searchText" +
                        "&page=$currentPage" +
                        "&pageSize=$pageSize"
                            .addConditionally(
                                categoryId != null,
                                categoryId?.split(",")?.joinToString(
                                    separator = "&categoriesId[]=",
                                    prefix = "&categoriesId[]="
                                ) ?: ""
                            )
                            .addConditionally(
                                !regionId.isNullOrEmpty(),
                                regionId?.split(",")?.joinToString(
                                    separator = "&regionsId[]=",
                                    prefix = "&regionsId[]="
                                ) ?: ""
                            )
                            .addConditionally(
                                vendorId != null,
                                "&vendorId=$vendorId"
                            )
                            .addConditionally(
                                from?.isNotEmpty() == true,
                                "&priceFrom=$from"
                            )
                            .addConditionally(
                                to?.isNotEmpty() == true,
                                "&priceTo=$to"
                            )
                            .addConditionally(
                                sortBy.isNotEmpty(),
                                "&sortBy=$sortBy"
                            )
                            .addConditionally(
                                sortType.isNotEmpty(),
                                "&sortDirection=$sortType"
                            )
            )
        )
    }
}

class ProductRepository internal constructor() {
    fun karikaProducts(
        pageSize: Int = 30,
        currentPage: Int,
        searchText: String = "",
        from: String = "",
        to: String = "",
        onlyWithImage: Boolean = false,
    ): Flow<ResultState<ProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ProductApi()
                .karikaProducts(
                    pageSize,
                    currentPage,
                    searchText,
                    from,
                    to,
                    onlyWithImage
                )
                .getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun promotedVendors(): Flow<ResultState<List<PromotedVendor>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ProductApi().promotedVendors().getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun searchProductsByCategory(
        categoryId: String? = null,
        regionId: String? = null,
        vendorId: Int? = null,
        from: String? = null,
        to: String? = null,
        pageSize: Int = 30,
        currentPage: Int = 1,
        searchText: String = "",
        sortBy: String = "",
        sortType: String = "",
    ): Flow<ResultState<List<Product>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ProductApi()
                .search(
                    categoryId,
                    regionId,
                    vendorId,
                    from,
                    to,
                    pageSize,
                    currentPage,
                    searchText,
                    sortBy,
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