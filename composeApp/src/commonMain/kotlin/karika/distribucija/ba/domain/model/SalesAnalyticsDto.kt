package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire DTOs for `GET /V1/vendor-operations/analytics/...` (Magento REST). Money and counts are
 * decimal strings with 4 fractional digits - parse with [toMoney]/[toCount], never as JSON floats.
 * Mapped to the plain UI-facing models in `SalesAnalytics.kt` inside [karika.distribucija.ba.domain.api.SalesAnalyticsRepository].
 */

fun String?.toMoney(): Double = this?.toDoubleOrNull() ?: 0.0
fun String?.toCount(): Int = this?.toDoubleOrNull()?.toInt() ?: 0

@Serializable
data class KpiComparisonDto(
    @SerialName("current") val current: String? = null,
    @SerialName("previous") val previous: String? = null,
    @SerialName("change_amount") val changeAmount: String? = null,
    @SerialName("change_percent") val changePercent: Double? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("basis") val basis: String? = null,
    @SerialName("available") val available: Boolean = false,
    @SerialName("zero_baseline") val zeroBaseline: Boolean = false,
)

@Serializable
data class KpiComparisonsDto(
    @SerialName("previous_period") val previousPeriod: KpiComparisonDto? = null,
    @SerialName("previous_month") val previousMonth: KpiComparisonDto? = null,
    @SerialName("previous_year") val previousYear: KpiComparisonDto? = null,
) {
    fun forBasis(basis: String): KpiComparisonDto? = when (basis) {
        "previous_month" -> previousMonth
        "previous_year" -> previousYear
        else -> previousPeriod
    }
}

@Serializable
data class KpiValueDto(
    @SerialName("value") val value: String? = null,
    @SerialName("has_data") val hasData: Boolean = false,
    @SerialName("comparisons") val comparisons: KpiComparisonsDto? = null,
) {
    fun deltaPercent(basis: String): Double = comparisons?.forBasis(basis)?.changePercent ?: 0.0
}

@Serializable
data class AnalyticsPeriodInfoDto(
    @SerialName("date_from") val dateFrom: String? = null,
    @SerialName("date_to") val dateTo: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("duration_days") val durationDays: Int = 0,
)

@Serializable
data class SalesSummaryDto(
    @SerialName("period") val period: AnalyticsPeriodInfoDto? = null,
    @SerialName("total_revenue") val totalRevenue: KpiValueDto = KpiValueDto(),
    @SerialName("order_count") val orderCount: KpiValueDto = KpiValueDto(),
    @SerialName("average_order_value") val averageOrderValue: KpiValueDto = KpiValueDto(),
    @SerialName("active_customers") val activeCustomers: KpiValueDto = KpiValueDto(),
    @SerialName("new_customers") val newCustomers: KpiValueDto = KpiValueDto(),
    @SerialName("revenue_growth") val revenueGrowth: KpiValueDto = KpiValueDto(),
    @SerialName("revenue_per_customer") val revenuePerCustomer: KpiValueDto = KpiValueDto(),
    @SerialName("average_orders_per_customer") val averageOrdersPerCustomer: KpiValueDto = KpiValueDto(),
    @SerialName("orphan_order_count") val orphanOrderCount: Int = 0,
    @SerialName("has_target") val hasTarget: Boolean = false,
    @SerialName("target_amount") val targetAmount: String? = null,
    @SerialName("target_achievement") val targetAchievement: KpiValueDto? = null,
    @SerialName("target_months_covered") val targetMonthsCovered: Int = 0,
    @SerialName("target_months_total") val targetMonthsTotal: Int = 0,
    @SerialName("customers_at_risk") val customersAtRisk: KpiValueDto = KpiValueDto(),
    @SerialName("distinct_products_sold") val distinctProductsSold: KpiValueDto = KpiValueDto(),
)

@Serializable
data class TrendPointDto(
    @SerialName("index") val index: Int = 0,
    @SerialName("value") val value: String? = null,
)

@Serializable
data class TrendBucketDto(
    @SerialName("index") val index: Int = 0,
    @SerialName("period") val period: String? = null,
    @SerialName("label") val label: String? = null,
)

@Serializable
data class TrendSeriesDto(
    @SerialName("unit") val unit: String? = null,
    @SerialName("has_data") val hasData: Boolean = false,
    @SerialName("points") val points: List<TrendPointDto> = emptyList(),
)

@Serializable
data class TrendSeriesMapDto(
    @SerialName("revenue") val revenue: TrendSeriesDto = TrendSeriesDto(),
    @SerialName("orders") val orders: TrendSeriesDto = TrendSeriesDto(),
    @SerialName("active_customers") val activeCustomers: TrendSeriesDto = TrendSeriesDto(),
    @SerialName("new_customers") val newCustomers: TrendSeriesDto = TrendSeriesDto(),
)

@Serializable
data class TrendResponseDto(
    @SerialName("group_by") val groupBy: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("buckets") val buckets: List<TrendBucketDto> = emptyList(),
    @SerialName("series") val series: TrendSeriesMapDto = TrendSeriesMapDto(),
)

@Serializable
data class RepresentativePerformanceDto(
    @SerialName("employee_id") val employeeId: Long = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("revenue") val revenue: String? = null,
    @SerialName("order_count") val orderCount: Int = 0,
    @SerialName("average_order_value") val averageOrderValue: String? = null,
    @SerialName("active_customers") val activeCustomers: Int = 0,
    @SerialName("new_customers") val newCustomers: Int = 0,
    @SerialName("revenue_growth") val revenueGrowth: KpiComparisonDto? = null,
    @SerialName("target_amount") val targetAmount: String? = null,
    @SerialName("target_achievement") val targetAchievement: String? = null,
    @SerialName("has_target") val hasTarget: Boolean = false,
    @SerialName("revenue_per_customer") val revenuePerCustomer: String? = null,
    @SerialName("assigned_customers") val assignedCustomers: Int = 0,
    @SerialName("repeat_purchase_rate") val repeatPurchaseRate: String? = null,
)

@Serializable
data class RepresentativePerformanceSearchResultsDto(
    @SerialName("items") val items: List<RepresentativePerformanceDto> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0,
    @SerialName("unassigned_revenue") val unassignedRevenue: String? = null,
)

@Serializable
data class ProductPerformanceDto(
    @SerialName("product_id") val productId: Long = 0,
    @SerialName("sku") val sku: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("revenue") val revenue: String? = null,
    @SerialName("quantity_sold") val quantitySold: Double = 0.0,
    @SerialName("order_count") val orderCount: Int = 0,
    @SerialName("growth") val growth: KpiComparisonDto? = null,
)

@Serializable
data class ProductPerformanceSearchResultsDto(
    @SerialName("items") val items: List<ProductPerformanceDto> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0,
)

@Serializable
data class CategoryPerformanceDto(
    @SerialName("category_id") val categoryId: Long = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("revenue") val revenue: String? = null,
    @SerialName("quantity_sold") val quantitySold: Double = 0.0,
    @SerialName("order_count") val orderCount: Int = 0,
    @SerialName("growth") val growth: KpiComparisonDto? = null,
)

@Serializable
data class CategoryPerformanceSearchResultsDto(
    @SerialName("items") val items: List<CategoryPerformanceDto> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0,
)

@Serializable
data class CustomerAnalyticsDto(
    @SerialName("assigned_customers") val assignedCustomers: Int = 0,
    @SerialName("active_customers") val activeCustomers: Int = 0,
    @SerialName("purchasing_customers") val purchasingCustomers: Int = 0,
    @SerialName("new_customers") val newCustomers: Int = 0,
    @SerialName("repeat_customers") val repeatCustomers: Int = 0,
    @SerialName("lifetime_repeat_customers") val lifetimeRepeatCustomers: Int = 0,
    @SerialName("never_ordered_customers") val neverOrderedCustomers: Int = 0,
    @SerialName("repeat_purchase_rate") val repeatPurchaseRate: KpiValueDto = KpiValueDto(),
    @SerialName("revenue_per_customer") val revenuePerCustomer: KpiValueDto = KpiValueDto(),
    @SerialName("average_orders_per_customer") val averageOrdersPerCustomer: KpiValueDto = KpiValueDto(),
)

@Serializable
data class CustomerRiskLevelHistogramDto(
    @SerialName("normal") val normal: Int = 0,
    @SerialName("approaching_risk") val approachingRisk: Int = 0,
    @SerialName("at_risk") val atRisk: Int = 0,
    @SerialName("severely_overdue") val severelyOverdue: Int = 0,
    @SerialName("never_ordered") val neverOrdered: Int = 0,
)

@Serializable
data class CustomerRiskSummaryDto(
    @SerialName("inactive30_plus") val inactive30Plus: Int = 0,
    @SerialName("inactive60_plus") val inactive60Plus: Int = 0,
    @SerialName("inactive90_plus") val inactive90Plus: Int = 0,
    @SerialName("inactive30_to59") val inactive30To59: Int = 0,
    @SerialName("inactive60_to89") val inactive60To89: Int = 0,
    @SerialName("never_ordered") val neverOrdered: Int = 0,
    @SerialName("by_risk_level") val byRiskLevel: CustomerRiskLevelHistogramDto = CustomerRiskLevelHistogramDto(),
    @SerialName("cycle_eligible_customers") val cycleEligibleCustomers: Int = 0,
)

@Serializable
data class CustomerRiskEntryDto(
    @SerialName("customer_id") val customerId: Long = 0,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("employee_id") val employeeId: Long? = null,
    @SerialName("employee_name") val employeeName: String? = null,
    @SerialName("employee_status") val employeeStatus: String? = null,
    @SerialName("last_order_at") val lastOrderAt: String? = null,
    @SerialName("days_inactive") val daysInactive: Int? = null,
    @SerialName("order_count_lifetime") val orderCountLifetime: Int = 0,
    @SerialName("average_order_interval_days") val averageOrderIntervalDays: Double? = null,
    @SerialName("median_order_interval_days") val medianOrderIntervalDays: Double? = null,
    @SerialName("average_order_value") val averageOrderValue: String? = null,
    @SerialName("lifetime_revenue") val lifetimeRevenue: String? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("overdue_ratio") val overdueRatio: Double? = null,
    @SerialName("detection_method") val detectionMethod: String? = null,
)

@Serializable
data class CustomerRiskEntrySearchResultsDto(
    @SerialName("items") val items: List<CustomerRiskEntryDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)
