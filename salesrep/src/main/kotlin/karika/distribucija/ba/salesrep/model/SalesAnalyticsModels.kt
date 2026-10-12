package karika.distribucija.ba.salesrep.model

/** Plain UI-facing analytics models, mirroring composeApp's domain/model/SalesAnalytics.kt. */

/**
 * One `/analytics/summary` KPI. [value] is null when the API sent no value (rendered as "—");
 * [deltaPercent] is null when the selected comparison is unavailable or has no change_percent
 * (rendered as "–"), so a missing comparison is never shown as a fake 0%.
 */
data class AnalyticsKpi(
    val value: Double? = null,
    val deltaPercent: Double? = null,
)

data class AnalyticsOverview(
    val totalRevenue: AnalyticsKpi = AnalyticsKpi(),
    val totalOrders: AnalyticsKpi = AnalyticsKpi(),
    val avgOrderValue: AnalyticsKpi = AnalyticsKpi(),
    val activeCustomers: AnalyticsKpi = AnalyticsKpi(),
    val newCustomers: AnalyticsKpi = AnalyticsKpi(),
    val distinctProducts: AnalyticsKpi = AnalyticsKpi(),
    val revenueGrowth: AnalyticsKpi = AnalyticsKpi(),
    val hasTarget: Boolean = false,
    val goalAchievement: AnalyticsKpi = AnalyticsKpi(),
    val atRiskCustomers: Double? = null,
)

/** One `/analytics/trends` series, one value per bucket (index-aligned with [AnalyticsTrends.labels]). */
data class AnalyticsTrendSeries(
    val hasData: Boolean = false,
    val values: List<Double> = emptyList(),
)

data class AnalyticsTrends(
    val labels: List<String> = emptyList(),
    val revenue: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val orders: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val activeCustomers: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val newCustomers: AnalyticsTrendSeries = AnalyticsTrendSeries(),
)

data class SalesRepPerformance(
    val name: String,
    val revenue: Double,
    val goalPercent: Double,
    val orders: Int,
    val avgOrderValue: Double,
    val activeCustomers: Int,
    val totalCustomers: Int,
    val growthPercent: Double,
    val suspended: Boolean = false,
)

data class SalesRepsPerformance(
    val reps: List<SalesRepPerformance> = emptyList(),
    val unassignedRevenue: Double = 0.0,
)

/** `/analytics/customers`, null-safe: a null field means the API sent no value ("—"). */
data class CustomerAnalytics(
    val assignedCustomers: Int? = null,
    val activeCustomers: Int? = null,
    val newCustomers: Int? = null,
    val repeatCustomers: Int? = null,
    val repeatPurchaseRate: Double? = null,
    val revenuePerCustomer: Double? = null,
    val avgOrdersPerCustomer: Double? = null,
    val customersWith2PlusOrders: Int? = null,
    val customersWithoutOrders: Int? = null,
)

enum class CustomerRiskLevel {
    APPROACHING_RISK,
    AT_RISK,
    SERIOUSLY_OVERDUE,
    NEVER_ORDERED,
}

data class AtRiskCustomer(
    val displayId: String,
    val name: String,
    val riskLevel: CustomerRiskLevel,
    val note: String? = null,
    val suspended: Boolean = false,
    val inactiveDays: Int,
    val lastOrderDate: String,
    val avgOrderValue: Double,
    val turnover: Double,
)

data class AtRiskCustomers(
    val bucket30to59: Int = 0,
    val bucket60to89: Int = 0,
    val bucket90plus: Int = 0,
    val customers: List<AtRiskCustomer> = emptyList(),
)

data class AnalyticsProduct(
    val name: String,
    val sku: String,
    val qty: Int,
    val orders: Int,
    val revenue: Double,
    val growthPercent: Double,
)

data class AnalyticsCategory(
    val name: String,
    val revenue: Double,
    val qty: Int,
    val orders: Int,
    val growthPercent: Double,
)
