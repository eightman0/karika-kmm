package karika.distribucija.ba.salesrep.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import karika.distribucija.ba.salesrep.network.HttpClientProvider

/** Raw GET calls for `/V1/vendor-operations/analytics/...`, mirroring composeApp's
 * domain/api/SalesAnalyticsApi.kt (see SalesAnalyticsRepository for parsing/mapping). */
internal class SalesAnalyticsApi {

    /** GET /V1/vendor-operations/analytics/summary */
    suspend fun getSummary(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/summary")) {
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId)
        }
    }

    /** GET /V1/vendor-operations/analytics/trends */
    suspend fun getTrends(
        dateFrom: String? = null,
        dateTo: String? = null,
        groupBy: String,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/trends")) {
            parameter("groupBy", groupBy)
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId)
        }
    }

    /** GET /V1/vendor-operations/analytics/representatives */
    suspend fun getRepresentatives(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/representatives")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            parameter("searchCriteria[sort_orders][0][field]", "revenue")
            parameter("searchCriteria[sort_orders][0][direction]", "DESC")
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId)
        }
    }

    /** GET /V1/vendor-operations/analytics/customers */
    suspend fun getCustomerAnalytics(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/customers")) {
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId)
        }
    }

    /** GET /V1/vendor-operations/analytics/risk-summary */
    suspend fun getRiskSummary(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/risk-summary")) {
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, null, null)
        }
    }

    /** GET /V1/vendor-operations/analytics/customers-at-risk */
    suspend fun getCustomersAtRisk(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        riskLevel: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/customers-at-risk")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            if (riskLevel != null) parameter("riskLevel", riskLevel)
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, null, null)
        }
    }

    /** GET /V1/vendor-operations/analytics/products */
    suspend fun getProducts(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        categoryId: Long? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/products")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            parameter("searchCriteria[sort_orders][0][field]", "revenue")
            parameter("searchCriteria[sort_orders][0][direction]", "DESC")
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, null, categoryId)
        }
    }

    /** GET /V1/vendor-operations/analytics/categories */
    suspend fun getCategories(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("vendor-operations/analytics/categories")) {
            parameter("searchCriteria[current_page]", page)
            parameter("searchCriteria[page_size]", pageSize)
            parameter("searchCriteria[sort_orders][0][field]", "revenue")
            parameter("searchCriteria[sort_orders][0][direction]", "DESC")
            applyCommonFilters(dateFrom, dateTo, salesRepresentativeId, customerId, productId, null)
        }
    }

    private fun HttpRequestBuilder.applyCommonFilters(
        dateFrom: String?,
        dateTo: String?,
        salesRepresentativeId: Long?,
        customerId: Long?,
        productId: Long?,
        categoryId: Long?,
    ) {
        if (dateFrom != null) parameter("dateFrom", dateFrom)
        if (dateTo != null) parameter("dateTo", dateTo)
        if (salesRepresentativeId != null) parameter("salesRepresentativeId", salesRepresentativeId)
        if (customerId != null) parameter("customerId", customerId)
        if (productId != null) parameter("productId", productId)
        if (categoryId != null) parameter("categoryId", categoryId)
    }
}
