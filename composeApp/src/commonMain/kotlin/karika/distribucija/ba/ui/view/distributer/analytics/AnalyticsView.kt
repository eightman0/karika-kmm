package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AnalyticsOverview
import karika.distribucija.ba.domain.model.AnalyticsTrends
import karika.distribucija.ba.domain.model.CustomerAnalytics
import karika.distribucija.ba.domain.model.SalesRepPerformance
import karika.distribucija.ba.domain.model.SalesRepsPerformance
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.XSpacer8
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_filter_outline
import karikav2.composeapp.generated.resources.ic_inventory
import karikav2.composeapp.generated.resources.ic_order_total
import karikav2.composeapp.generated.resources.ic_total
import karikav2.composeapp.generated.resources.ic_warning
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AnalyticsView(component: AnalyticsComponent) {
    val selectedTab by component.selectedTab.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            text = "Analitika — ${selectedTab.title()}",
            color = KarikaColors.Gray2,
            textSize = 20.sp,
            fontWeight = FontWeight.W700
        )

        AnalyticsFilterBar(component)

        when (selectedTab) {
            AnalyticsTab.Overview -> OverviewTab(component)
            AnalyticsTab.Trends -> TrendsTab(component)
            AnalyticsTab.Reps -> RepsTab(component)
            AnalyticsTab.Customers -> CustomersTab(component)
        }
    }
}

private fun AnalyticsTab.title() = when (this) {
    AnalyticsTab.Overview -> "Pregled"
    AnalyticsTab.Trends -> "Trendovi prodaje"
    AnalyticsTab.Reps -> "Komercijalisti"
    AnalyticsTab.Customers -> "Analitika kupaca"
}

@Composable
private fun AnalyticsFilterBar(component: AnalyticsComponent) {
    val dateFrom by component.filters.dateFrom
    val dateTo by component.filters.dateTo
    val grouping by component.filters.grouping
    val comparison by component.filters.comparison

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(16.dp))
            .onClick(callback = component::openFilters)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = vectorResource(Res.drawable.ic_filter_outline),
            tint = KarikaColors.Gray7,
            contentDescription = ""
        )
        XSpacer8()
        KarikaText(
            modifier = Modifier.weight(1f),
            text = "${dateFrom.trimEnd('.')} – ${dateTo.trimEnd('.')} · $grouping · $comparison",
            color = KarikaColors.Gray2,
            textSize = 12.sp,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = vectorResource(Res.drawable.ic_arrow_down),
            tint = KarikaColors.Gray7,
            contentDescription = ""
        )
    }
}

@Composable
fun AnalyticsEmptyState(loader: Boolean, text: String = "Nema rezultata za odabrane filtere.") {
    if (loader) return
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = text,
            color = KarikaColors.Gray7,
            textSize = 14.sp,
            fontWeight = FontWeight.W500
        )
    }
}

@Composable
private fun DeltaText(delta: Double, suffix: String = "vs prethodni period") {
    val positive = delta >= 0
    val arrow = if (positive) "↑" else "↓"
    val color = if (positive) KarikaColors.Green3 else KarikaColors.Red
    KarikaText(
        text = "$arrow ${if (positive) "+" else ""}${formatNumber(delta)}% $suffix",
        color = color,
        textSize = 12.sp,
        fontWeight = FontWeight.W500
    )
}

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    delta: Double,
    icon: DrawableResource,
    iconBackground: Color
) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KarikaText(text = title, color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W400)
                KarikaText(text = value, color = KarikaColors.Gray2, textSize = 22.sp, fontWeight = FontWeight.W700)
                DeltaText(delta)
            }
            Column(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = vectorResource(icon),
                    tint = KarikaColors.Gray2,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(component: AnalyticsComponent) {
    val overview by component.overview.collectAsState()

    StatCard(
        title = "Ukupan prihod:",
        value = "${karikaPriceFormat(overview.totalRevenue)} KM",
        delta = overview.totalRevenueDelta,
        icon = Res.drawable.ic_total,
        iconBackground = KarikaColors.Green4
    )
    StatCard(
        title = "Ukupan broj narudžbi:",
        value = "${overview.totalOrders}",
        delta = overview.totalOrdersDelta,
        icon = Res.drawable.ic_order_total,
        iconBackground = KarikaColors.Green4
    )
    StatCard(
        title = "Prosječna vrijednost narudžbe:",
        value = "${karikaPriceFormat(overview.avgOrderValue)} KM",
        delta = overview.avgOrderValueDelta,
        icon = Res.drawable.ic_inventory,
        iconBackground = KarikaColors.MineMessage.copy(alpha = 0.12f)
    )

    Card {
        KarikaText(text = "Kupci u periodu", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
        Row(modifier = Modifier.fillMaxWidth()) {
            OverviewMetric(modifier = Modifier.weight(1f), value = "${overview.activeCustomers}", label = "Aktivni kupci", delta = overview.activeCustomersDelta)
            OverviewMetric(modifier = Modifier.weight(1f), value = "${overview.newCustomers}", label = "Novi kupci", delta = overview.newCustomersDelta)
        }
        YSpacer8()
        Row(modifier = Modifier.fillMaxWidth()) {
            OverviewMetric(modifier = Modifier.weight(1f), value = "${overview.distinctProducts}", label = "Različiti proizvodi", delta = overview.distinctProductsDelta)
            OverviewMetric(modifier = Modifier.weight(1f), value = "${formatNumber(overview.revenueGrowth)}%", label = "Rast prihoda", delta = overview.revenueGrowthDelta)
        }
    }

    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            KarikaText(text = "Ostvarenje cilja:", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W400)
            KarikaText(text = "${formatNumber(overview.goalAchievement)}%", color = KarikaColors.Gray2, textSize = 20.sp, fontWeight = FontWeight.W700)
        }
        AnalyticsProgressBar(progress = (overview.goalAchievement / 100f).toFloat())
        DeltaText(overview.goalAchievementDelta)
    }

    Card(modifier = Modifier.onClick { component.openAtRiskCustomers() }) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KarikaText(text = "Kupci u riziku:", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W400)
                KarikaText(text = "${overview.atRiskCustomers}", color = KarikaColors.Gray2, textSize = 22.sp, fontWeight = FontWeight.W700)
                KarikaText(text = "30+ dana bez narudžbe", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
            }
            Column(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(KarikaColors.Yellow1),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = vectorResource(Res.drawable.ic_warning),
                    tint = KarikaColors.Yellow2,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun OverviewMetric(modifier: Modifier = Modifier, value: String, label: String, delta: Double) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        KarikaText(text = value, color = KarikaColors.Gray2, textSize = 20.sp, fontWeight = FontWeight.W700)
        KarikaText(text = label, color = KarikaColors.Gray2, textSize = 13.sp, fontWeight = FontWeight.W400)
        DeltaText(delta, suffix = "")
    }
}

@Composable
private fun TrendsTab(component: AnalyticsComponent) {
    val trends by component.trends.collectAsState()

    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            KarikaText(text = "Prihod", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaText(text = "po danu", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
        }
        AnalyticsAreaChart(points = trends.revenueSeries)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TrendFooterStat("Ukupno", "${karikaPriceFormat(trends.revenueTotal)} KM")
            TrendFooterStat("Vrh", "${karikaPriceFormat(trends.revenuePeak)} KM")
            TrendFooterStat("Rast", "+${formatNumber(trends.revenueGrowth)}%", KarikaColors.Green3)
        }
    }

    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            KarikaText(text = "Narudžbe", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaText(text = "${trends.ordersTotal}", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
        }
        AnalyticsStepChart(points = trends.ordersSeries)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.weight(1f)) {
            KarikaText(text = "Aktivni kupci", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W600)
            KarikaText(text = "${trends.activeCustomers}", color = KarikaColors.Gray2, textSize = 20.sp, fontWeight = FontWeight.W700)
            AnalyticsStepChart(points = trends.activeCustomersSeries, showAxisLabels = false, modifier = Modifier.fillMaxWidth().height(40.dp))
        }
        Card(modifier = Modifier.weight(1f)) {
            KarikaText(text = "Novi kupci", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W600)
            KarikaText(text = "${trends.newCustomers}", color = KarikaColors.Gray2, textSize = 20.sp, fontWeight = FontWeight.W700)
            AnalyticsStepChart(points = trends.newCustomersSeries, showAxisLabels = false, modifier = Modifier.fillMaxWidth().height(40.dp))
        }
    }
}

@Composable
private fun TrendFooterStat(label: String, value: String, color: Color = KarikaColors.Gray2) {
    Column {
        KarikaText(text = label, color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
        KarikaText(text = value, color = color, textSize = 14.sp, fontWeight = FontWeight.W700)
    }
}

@Composable
private fun RepsTab(component: AnalyticsComponent) {
    val data by component.reps.collectAsState()
    val loader by component.loader.collectAsState()

    KarikaText(text = "Učinak komercijalista", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)

    if (data.reps.isEmpty()) {
        AnalyticsEmptyState(loader)
    }

    data.reps.forEachIndexed { index, rep ->
        RepCard(index = index + 1, rep = rep)
    }

    Card {
        KarikaText(text = "Nedodijeljeno", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W700)
        KarikaText(text = "Prihod kupaca bez aktivne dodjele", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
        KarikaText(text = "${karikaPriceFormat(data.unassignedRevenue)} KM", color = KarikaColors.Gray2, textSize = 18.sp, fontWeight = FontWeight.W700)
        KarikaText(
            text = "Prihod prati trenutnu dodjelu kupca. Promjena dodjele prenosi cijelu historiju narudžbi na novog komercijalistu.",
            color = KarikaColors.Gray7,
            textSize = 11.sp,
            fontWeight = FontWeight.W400
        )
    }
}

private fun goalColor(goalPercent: Double): Color = when {
    goalPercent >= 100.0 -> KarikaColors.Green3
    goalPercent >= 70.0 -> KarikaColors.Orange
    else -> KarikaColors.Red
}

@Composable
private fun RepCard(index: Int, rep: SalesRepPerformance) {
    Card {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(KarikaColors.Gray20),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KarikaText(text = "$index", color = KarikaColors.Gray2, textSize = 12.sp, fontWeight = FontWeight.W700)
            }
            XSpacer8()
            KarikaText(text = rep.name, color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W700)
            if (rep.suspended) {
                XSpacer8()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KarikaColors.Gray20)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    KarikaText(text = "Suspendovan", color = KarikaColors.Gray2, textSize = 10.sp, fontWeight = FontWeight.W600)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            KarikaText(text = "${karikaPriceFormat(rep.revenue)} KM", color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W700)
        }
        YSpacer8()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AnalyticsProgressBar(
                modifier = Modifier.weight(1f).height(8.dp),
                progress = (rep.goalPercent / 100f).toFloat(),
                progressColor = goalColor(rep.goalPercent)
            )
            XSpacer8()
            KarikaText(text = "cilj ${formatNumber(rep.goalPercent)}%", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W500)
        }
        YSpacer8()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RepStat("Narudžbe", "${rep.orders}")
            RepStat("AOV", "${karikaPriceFormat(rep.avgOrderValue)} KM")
            RepStat("Kupci", "${rep.activeCustomers} / ${rep.totalCustomers}")
            RepStat(
                "Rast",
                "${if (rep.growthPercent >= 0) "+" else ""}${formatNumber(rep.growthPercent)}%",
                if (rep.growthPercent >= 0) KarikaColors.Green3 else KarikaColors.Red
            )
        }
    }
}

@Composable
private fun RepStat(label: String, value: String, color: Color = KarikaColors.Gray2) {
    Column {
        KarikaText(text = label, color = KarikaColors.Gray7, textSize = 11.sp, fontWeight = FontWeight.W400)
        KarikaText(text = value, color = color, textSize = 13.sp, fontWeight = FontWeight.W700)
    }
}

@Composable
private fun CustomersTab(component: AnalyticsComponent) {
    val customers by component.customers.collectAsState()

    KarikaText(text = "U PERIODU", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W600)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.assignedCustomers}", label = "Dodijeljeni kupci")
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.activeCustomers}", label = "Aktivni kupci")
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.newCustomers}", label = "Novi kupci")
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.repeatCustomers}", label = "Ponavljajući kupci")
    }

    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            KarikaText(text = "Stopa ponovljene kupovine:", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W400)
            KarikaText(text = "${formatNumber(customers.repeatPurchaseRate)}%", color = KarikaColors.Gray2, textSize = 18.sp, fontWeight = FontWeight.W700)
        }
        AnalyticsProgressBar(progress = (customers.repeatPurchaseRate / 100f).toFloat())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                KarikaText(text = "${karikaPriceFormat(customers.revenuePerCustomer)} KM", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
                KarikaText(text = "Prihod po kupcu", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
            }
            Column {
                KarikaText(text = formatNumber(customers.avgOrdersPerCustomer), color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
                KarikaText(text = "Prosječan broj narudžbi po kupcu", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W400)
            }
        }
    }

    KarikaText(
        text = "Kupac može biti dodijeljen više komercijalista, pa se portfelji mogu preklapati.",
        color = KarikaColors.Gray7,
        textSize = 11.sp,
        fontWeight = FontWeight.W400
    )

    YSpacer16()
    KarikaText(text = "UKUPNO", color = KarikaColors.Gray7, textSize = 12.sp, fontWeight = FontWeight.W600)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.customersWith2PlusOrders}", label = "Kupci sa 2+ narudžbe")
        SimpleStatCard(modifier = Modifier.weight(1f), value = "${customers.customersWithoutOrders}", label = "Kupci bez narudžbe")
    }
}

@Composable
private fun SimpleStatCard(modifier: Modifier = Modifier, value: String, label: String) {
    Card(modifier = modifier) {
        KarikaText(text = value, color = KarikaColors.Gray2, textSize = 22.sp, fontWeight = FontWeight.W700)
        KarikaText(text = label, color = KarikaColors.Gray2, textSize = 13.sp, fontWeight = FontWeight.W400)
    }
}
