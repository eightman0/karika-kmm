package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.model.AnalyticsCategory
import karika.distribucija.ba.domain.model.AnalyticsOverview
import karika.distribucija.ba.domain.model.AnalyticsProduct
import karika.distribucija.ba.domain.model.AnalyticsSeriesPoint
import karika.distribucija.ba.domain.model.AnalyticsTrendSeries
import karika.distribucija.ba.domain.model.AnalyticsTrends
import karika.distribucija.ba.domain.model.AtRiskCustomer
import karika.distribucija.ba.domain.model.AtRiskCustomers
import karika.distribucija.ba.domain.model.CategoryPerformanceSearchResultsDto
import karika.distribucija.ba.domain.model.CustomerAnalytics
import karika.distribucija.ba.domain.model.CustomerAnalyticsDto
import karika.distribucija.ba.domain.model.CustomerRiskEntryDto
import karika.distribucija.ba.domain.model.CustomerRiskEntrySearchResultsDto
import karika.distribucija.ba.domain.model.CustomerRiskLevel
import karika.distribucija.ba.domain.model.CustomerRiskSummaryDto
import karika.distribucija.ba.domain.model.ErrorResponse
import karika.distribucija.ba.domain.model.ProductPerformanceSearchResultsDto
import karika.distribucija.ba.domain.model.RepresentativePerformanceSearchResultsDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SalesRepPerformance
import karika.distribucija.ba.domain.model.SalesRepsPerformance
import karika.distribucija.ba.domain.model.SalesSummaryDto
import karika.distribucija.ba.domain.model.TrendBucketDto
import karika.distribucija.ba.domain.model.TrendResponseDto
import karika.distribucija.ba.domain.model.TrendSeriesDto
import karika.distribucija.ba.domain.model.toCount
import karika.distribucija.ba.domain.model.toMoney
import karika.distribucija.ba.domain.model.toMoneyOrNull
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Backs the "Analitika" section with the real `/V1/vendor-operations/analytics/...` Magento
 * endpoints. Each method parses the wire DTOs from [karika.distribucija.ba.domain.model] and maps
 * them onto the plain UI-facing models in `SalesAnalytics.kt`, so the analytics components/views
 * did not need to change when this was swapped in from the previous hardcoded version.
 *
 * `comparisonBasis` is one of `previous_period` | `previous_month` | `previous_year` - it just
 * selects which [karika.distribucija.ba.domain.model.KpiComparisonsDto] bucket the delta/growth
 * numbers are read from, the server always returns all three.
 */
class SalesAnalyticsRepository internal constructor() {

    private val api = SalesAnalyticsApi()

    fun getOverview(
        dateFrom: String? = null,
        dateTo: String? = null,
        comparisonBasis: String = "previous_period",
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Flow<ResultState<AnalyticsOverview>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getSummary(
                dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<SalesSummaryDto>()
                emit(ResultState.Success(dto.toAnalyticsOverview(comparisonBasis)))
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getTrends(
        dateFrom: String? = null,
        dateTo: String? = null,
        groupBy: String = "day",
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Flow<ResultState<AnalyticsTrends>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getTrends(
                dateFrom, dateTo, groupBy, salesRepresentativeId, customerId, productId, categoryId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<TrendResponseDto>()
                emit(ResultState.Success(dto.toAnalyticsTrends()))
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getReps(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Flow<ResultState<SalesRepsPerformance>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getRepresentatives(
                dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<RepresentativePerformanceSearchResultsDto>()
                emit(ResultState.Success(dto.toSalesRepsPerformance()))
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCustomerAnalytics(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
        categoryId: Long? = null,
    ): Flow<ResultState<CustomerAnalytics>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getCustomerAnalytics(
                dateFrom, dateTo, salesRepresentativeId, customerId, productId, categoryId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<CustomerAnalyticsDto>()
                emit(ResultState.Success(dto.toCustomerAnalytics()))
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    /** Combines `/risk-summary` (bucket counts) with two `/customers-at-risk` pages (default 30+
     * days list, plus the never-ordered list the default query excludes) into one screen model. */
    fun getAtRiskCustomers(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
    ): Flow<ResultState<AtRiskCustomers>> = flow {
        emit(ResultState.Loading)
        try {
            val summaryResponse = api.getRiskSummary(
                dateFrom, dateTo, salesRepresentativeId, customerId
            ).getOrNoInternet()
            if (summaryResponse.status != HttpStatusCode.OK) {
                emit(ResultState.Error(summaryResponse.body<ErrorResponse>().message))
                return@flow
            }
            val summary = summaryResponse.body<CustomerRiskSummaryDto>()

            val overdueResponse = api.getCustomersAtRisk(
                dateFrom, dateTo, salesRepresentativeId, customerId
            ).getOrNoInternet()
            if (overdueResponse.status != HttpStatusCode.OK) {
                emit(ResultState.Error(overdueResponse.body<ErrorResponse>().message))
                return@flow
            }
            val overdue = overdueResponse.body<CustomerRiskEntrySearchResultsDto>()

            val neverOrderedResponse = api.getCustomersAtRisk(
                dateFrom, dateTo, salesRepresentativeId, customerId, riskLevel = "never_ordered"
            ).getOrNoInternet()
            if (neverOrderedResponse.status != HttpStatusCode.OK) {
                emit(ResultState.Error(neverOrderedResponse.body<ErrorResponse>().message))
                return@flow
            }
            val neverOrdered = neverOrderedResponse.body<CustomerRiskEntrySearchResultsDto>()

            emit(
                ResultState.Success(
                    AtRiskCustomers(
                        bucket30to59 = summary.inactive30To59,
                        bucket60to89 = summary.inactive60To89,
                        bucket90plus = summary.inactive90Plus,
                        customers = (overdue.items + neverOrdered.items).map { it.toAtRiskCustomer() },
                    )
                )
            )
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getProducts(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        categoryId: Long? = null,
    ): Flow<ResultState<List<AnalyticsProduct>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getProducts(
                dateFrom, dateTo, salesRepresentativeId, customerId, categoryId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<ProductPerformanceSearchResultsDto>()
                emit(
                    ResultState.Success(
                        dto.items.map { item ->
                            AnalyticsProduct(
                                name = item.name ?: "",
                                sku = item.sku ?: "",
                                qty = item.quantitySold.toInt(),
                                orders = item.orderCount,
                                revenue = item.revenue.toMoney(),
                                growthPercent = item.growth?.changePercent ?: 0.0,
                            )
                        }
                    )
                )
                return@flow
            }
            emit(ResultState.Error(response.body<ErrorResponse>().message))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCategories(
        dateFrom: String? = null,
        dateTo: String? = null,
        salesRepresentativeId: Long? = null,
        customerId: Long? = null,
        productId: Long? = null,
    ): Flow<ResultState<List<AnalyticsCategory>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = api.getCategories(
                dateFrom, dateTo, salesRepresentativeId, customerId, productId
            ).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<CategoryPerformanceSearchResultsDto>()
                emit(
                    ResultState.Success(
                        dto.items.map { item ->
                            AnalyticsCategory(
                                name = item.name ?: "",
                                revenue = item.revenue.toMoney(),
                                qty = item.quantitySold.toInt(),
                                orders = item.orderCount,
                                growthPercent = item.growth?.changePercent ?: 0.0,
                            )
                        }
                    )
                )
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

private fun SalesSummaryDto.toAnalyticsOverview(basis: String) = AnalyticsOverview(
    totalRevenue = totalRevenue.value.toMoney(),
    totalRevenueDelta = totalRevenue.deltaPercent(basis),
    totalOrders = orderCount.value.toCount(),
    totalOrdersDelta = orderCount.deltaPercent(basis),
    avgOrderValue = averageOrderValue.value.toMoney(),
    avgOrderValueDelta = averageOrderValue.deltaPercent(basis),
    activeCustomers = activeCustomers.value.toCount(),
    activeCustomersDelta = activeCustomers.deltaPercent(basis),
    newCustomers = newCustomers.value.toCount(),
    newCustomersDelta = newCustomers.deltaPercent(basis),
    distinctProducts = distinctProductsSold.value.toCount(),
    distinctProductsDelta = distinctProductsSold.deltaPercent(basis),
    revenueGrowth = revenueGrowth.valueOrNull(),
    revenueGrowthDelta = revenueGrowth.deltaPercent(basis),
    hasTarget = hasTarget,
    goalAchievement = targetAchievement?.valueOrNull(),
    goalAchievementDelta = targetAchievement?.deltaPercent(basis),
    atRiskCustomers = customersAtRisk.value.toCount(),
)

private val bsShortMonths = listOf("jan", "feb", "mar", "apr", "maj", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

/** Axis label for a bucket like the web: `2026-09-13` -> "13. sep", `2026-09` -> "sep 2026";
 * anything else falls back to the API's own label. */
private fun TrendBucketDto.axisLabel(): String {
    val period = period.orEmpty()
    val parts = period.take(10).split('-')
    val month = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..12 }
    if (parts.size == 3 && month != null) {
        val day = parts[2].toIntOrNull()
        if (day != null) return "$day. ${bsShortMonths[month - 1]}"
    }
    if (parts.size == 2 && month != null) return "${bsShortMonths[month - 1]} ${parts[0]}"
    return label ?: period
}

private fun TrendResponseDto.toAnalyticsTrends(): AnalyticsTrends {
    val sortedBuckets = buckets.sortedBy { it.index }
    fun series(dto: TrendSeriesDto): AnalyticsTrendSeries {
        val byIndex = dto.points.associateBy { it.index }
        val points = if (sortedBuckets.isNotEmpty()) {
            sortedBuckets.map { bucket ->
                AnalyticsSeriesPoint(bucket.axisLabel(), byIndex[bucket.index]?.value.toMoney().toFloat())
            }
        } else {
            dto.points.sortedBy { it.index }.map { AnalyticsSeriesPoint("", it.value.toMoney().toFloat()) }
        }
        return AnalyticsTrendSeries(points = points, hasData = dto.hasData)
    }
    return AnalyticsTrends(
        revenue = series(series.revenue),
        orders = series(series.orders),
        activeCustomers = series(series.activeCustomers),
        newCustomers = series(series.newCustomers),
    )
}

private fun RepresentativePerformanceSearchResultsDto.toSalesRepsPerformance() = SalesRepsPerformance(
    reps = items.map { item ->
        SalesRepPerformance(
            name = item.name ?: "",
            revenue = item.revenue.toMoneyOrNull(),
            orders = item.orderCount,
            avgOrderValue = item.averageOrderValue.toMoneyOrNull(),
            activeCustomers = item.activeCustomers,
            newCustomers = item.newCustomers,
            growthPercent = item.revenueGrowth?.changePercent,
            goalPercent = if (item.hasTarget) item.targetAchievement.toMoneyOrNull() else null,
            revenuePerCustomer = item.revenuePerCustomer.toMoneyOrNull(),
            assignedCustomers = item.assignedCustomers,
            repeatPurchaseRate = item.repeatPurchaseRate.toMoneyOrNull(),
            suspended = item.status != null && item.status != "active",
        )
    },
    unassignedRevenue = unassignedRevenue.toMoneyOrNull(),
)

private fun CustomerAnalyticsDto.toCustomerAnalytics() = CustomerAnalytics(
    assignedCustomers = assignedCustomers,
    activeCustomers = activeCustomers,
    newCustomers = newCustomers,
    repeatCustomers = repeatCustomers,
    repeatPurchaseRate = repeatPurchaseRate.value.toMoneyOrNull(),
    revenuePerCustomer = revenuePerCustomer.value.toMoneyOrNull(),
    avgOrdersPerCustomer = averageOrdersPerCustomer.value.toMoneyOrNull(),
    customersWith2PlusOrders = lifetimeRepeatCustomers,
    customersWithoutOrders = neverOrderedCustomers,
)

private fun String?.toRiskLevel(): CustomerRiskLevel = when (this) {
    "approaching_risk" -> CustomerRiskLevel.APPROACHING_RISK
    "severely_overdue" -> CustomerRiskLevel.SERIOUSLY_OVERDUE
    "never_ordered" -> CustomerRiskLevel.NEVER_ORDERED
    else -> CustomerRiskLevel.AT_RISK
}

private fun CustomerRiskEntryDto.toAtRiskCustomer() = AtRiskCustomer(
    displayId = "Kupac $customerId",
    name = customerName ?: "",
    riskLevel = riskLevel.toRiskLevel(),
    note = if (detectionMethod == "cycle" && medianOrderIntervalDays != null) {
        "medijan ciklusa ${medianOrderIntervalDays.toInt()} dana"
    } else {
        null
    },
    suspended = employeeStatus != null && employeeStatus != "active",
    inactiveDays = daysInactive ?: 0,
    lastOrderDate = lastOrderAt?.let { runCatching { it.toDate1() }.getOrDefault(it) } ?: "-",
    avgOrderValue = averageOrderValue.toMoney(),
    turnover = lifetimeRevenue.toMoney(),
)
