package karika.distribucija.ba.domain.model

/**
 * Sales overview KPIs (`GET /analytics/summary`). A `*Delta` is the `change_percent` of the
 * selected comparison basis and stays `null` when that comparison is not available (or has a zero
 * baseline) - the UI renders it as "–", exactly like the web.
 */
data class AnalyticsOverview(
    val totalRevenue: Double = 0.0,
    val totalRevenueDelta: Double? = null,
    val totalOrders: Int = 0,
    val totalOrdersDelta: Double? = null,
    val avgOrderValue: Double = 0.0,
    val avgOrderValueDelta: Double? = null,
    val activeCustomers: Int = 0,
    val activeCustomersDelta: Double? = null,
    val newCustomers: Int = 0,
    val newCustomersDelta: Double? = null,
    val distinctProducts: Int = 0,
    val distinctProductsDelta: Double? = null,
    /** Percent; `null` when the API reports no data (shown as "—"). */
    val revenueGrowth: Double? = null,
    val revenueGrowthDelta: Double? = null,
    val hasTarget: Boolean = false,
    val goalAchievement: Double? = null,
    val goalAchievementDelta: Double? = null,
    val atRiskCustomers: Int = 0,
)

data class AnalyticsSeriesPoint(
    val label: String,
    val value: Float,
)

/** One trends series - one point per bucket of `/analytics/trends`, in bucket order. */
data class AnalyticsTrendSeries(
    val points: List<AnalyticsSeriesPoint> = emptyList(),
    val hasData: Boolean = false,
)

data class AnalyticsTrends(
    val revenue: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val orders: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val activeCustomers: AnalyticsTrendSeries = AnalyticsTrendSeries(),
    val newCustomers: AnalyticsTrendSeries = AnalyticsTrendSeries(),
)

/** One row of `GET /analytics/representatives`; `null` means the API returned no value ("—"). */
data class SalesRepPerformance(
    val name: String,
    val revenue: Double? = null,
    val orders: Int? = null,
    val avgOrderValue: Double? = null,
    val activeCustomers: Int? = null,
    val newCustomers: Int? = null,
    /** Percent; `null` when the comparison is not available. */
    val growthPercent: Double? = null,
    /** Percent; `null` when the rep has no target. */
    val goalPercent: Double? = null,
    val revenuePerCustomer: Double? = null,
    val assignedCustomers: Int? = null,
    /** Percent (0-100). */
    val repeatPurchaseRate: Double? = null,
    val suspended: Boolean = false,
)

data class SalesRepsPerformance(
    val reps: List<SalesRepPerformance> = emptyList(),
    val unassignedRevenue: Double? = null,
)

/** `GET /analytics/customers`; `null` means the API returned no value (shown as "—"). */
data class CustomerAnalytics(
    val assignedCustomers: Int? = null,
    val activeCustomers: Int? = null,
    val newCustomers: Int? = null,
    val repeatCustomers: Int? = null,
    /** Percent (0-100). */
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
