package karika.distribucija.ba.domain.model

data class AnalyticsOverview(
    val totalRevenue: Double = 0.0,
    val totalRevenueDelta: Double = 0.0,
    val totalOrders: Int = 0,
    val totalOrdersDelta: Double = 0.0,
    val avgOrderValue: Double = 0.0,
    val avgOrderValueDelta: Double = 0.0,
    val activeCustomers: Int = 0,
    val activeCustomersDelta: Double = 0.0,
    val newCustomers: Int = 0,
    val newCustomersDelta: Double = 0.0,
    val distinctProducts: Int = 0,
    val distinctProductsDelta: Double = 0.0,
    val revenueGrowth: Double = 0.0,
    val revenueGrowthDelta: Double = 0.0,
    val goalAchievement: Double = 0.0,
    val goalAchievementDelta: Double = 0.0,
    val atRiskCustomers: Int = 0,
)

data class AnalyticsSeriesPoint(
    val label: String,
    val value: Float,
)

data class AnalyticsTrends(
    val revenueSeries: List<AnalyticsSeriesPoint> = emptyList(),
    val revenueTotal: Double = 0.0,
    val revenuePeak: Double = 0.0,
    val revenueGrowth: Double = 0.0,
    val ordersSeries: List<AnalyticsSeriesPoint> = emptyList(),
    val ordersTotal: Int = 0,
    val activeCustomersSeries: List<AnalyticsSeriesPoint> = emptyList(),
    val activeCustomers: Int = 0,
    val newCustomersSeries: List<AnalyticsSeriesPoint> = emptyList(),
    val newCustomers: Int = 0,
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

data class CustomerAnalytics(
    val assignedCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val newCustomers: Int = 0,
    val repeatCustomers: Int = 0,
    val repeatPurchaseRate: Double = 0.0,
    val revenuePerCustomer: Double = 0.0,
    val avgOrdersPerCustomer: Double = 0.0,
    val customersWith2PlusOrders: Int = 0,
    val customersWithoutOrders: Int = 0,
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
