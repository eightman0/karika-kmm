package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AnalyticsTrendSeries
import karika.distribucija.ba.domain.model.SalesRepPerformance
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.XSpacer8
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_filter_outline
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

private val ValueCardShape = RoundedCornerShape(12.dp)

/** Columns of the analytics grids: [wide] on tablets/wide screens, [phone] otherwise. */
@Composable
private fun AnalyticsGrid(
    phone: Int,
    wide: Int,
    equalHeight: Boolean = true,
    items: List<@Composable (Modifier) -> Unit>,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 600.dp) wide else phone
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (equalHeight) Modifier.height(IntrinsicSize.Min) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { item ->
                        item(Modifier.weight(1f).then(if (equalHeight) Modifier.fillMaxHeight() else Modifier))
                    }
                    repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun WhiteCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(ValueCardShape)
            .background(KarikaColors.White)
            .border(width = 1.dp, color = KarikaUiColors.Border, shape = ValueCardShape)
            .then(if (onClick != null) Modifier.onClick(callback = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

/**
 * KPI card like the web: big bold value, optional muted [caption] under it, the label, and - with
 * [showDelta] - a right-aligned delta ("+12,5%" / "−8%" / "0%" / "–") with [deltaSuffix] under it.
 */
@Composable
fun AnalyticsValueCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    note: String? = null,
    showDelta: Boolean = false,
    delta: Double? = null,
    deltaSuffix: String = "vs prethodni period",
    onClick: (() -> Unit)? = null,
) {
    WhiteCard(modifier = modifier, onClick = onClick) {
        KarikaText(text = value, color = KarikaUiColors.Ink, textSize = 22.sp, fontWeight = FontWeight.W700, maxLines = 1)
        if (caption != null) {
            KarikaText(text = caption, color = KarikaUiColors.Muted, textSize = 11.sp, fontWeight = FontWeight.W400)
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = label,
                color = KarikaUiColors.Ink,
                textSize = 13.sp,
                fontWeight = FontWeight.W500,
                lineHeight = 16.sp
            )
            if (showDelta) {
                XSpacer8()
                val deltaText = formatDelta(delta)
                val deltaColor = when {
                    deltaText.startsWith("+") -> KarikaUiColors.Green
                    deltaText.startsWith("−") -> KarikaUiColors.Red
                    else -> KarikaUiColors.Muted
                }
                Column(horizontalAlignment = Alignment.End) {
                    KarikaText(text = deltaText, color = deltaColor, textSize = 12.sp, fontWeight = FontWeight.W600, maxLines = 1)
                    KarikaText(
                        text = deltaSuffix,
                        color = KarikaUiColors.Subtle,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.End,
                        lineHeight = 12.sp
                    )
                }
            }
        }
        if (note != null) {
            KarikaText(text = note, color = KarikaUiColors.Muted, textSize = 11.sp, fontWeight = FontWeight.W400, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun SectionCaption(text: String) {
    KarikaText(text = text, color = KarikaUiColors.Muted, textSize = 12.sp, fontWeight = FontWeight.W600)
}

@Composable
private fun OverviewTab(component: AnalyticsComponent) {
    val overview by component.overview.collectAsState()
    val comparison by component.filters.comparison
    val suffix = "vs ${comparison.lowercase()}"

    val cards = buildList<@Composable (Modifier) -> Unit> {
        add { m -> AnalyticsValueCard(modifier = m, value = formatMoney(overview.totalRevenue), label = "Ukupan prihod", showDelta = true, delta = overview.totalRevenueDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = "${overview.totalOrders}", label = "Narudžbe", showDelta = true, delta = overview.totalOrdersDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = formatMoney(overview.avgOrderValue), label = "Prosječna vrijednost narudžbe", showDelta = true, delta = overview.avgOrderValueDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = "${overview.activeCustomers}", label = "Aktivni kupci", showDelta = true, delta = overview.activeCustomersDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = "${overview.newCustomers}", label = "Novi kupci", showDelta = true, delta = overview.newCustomersDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = "${overview.distinctProducts}", label = "Različiti proizvodi", showDelta = true, delta = overview.distinctProductsDelta, deltaSuffix = suffix) }
        add { m -> AnalyticsValueCard(modifier = m, value = formatPercentOrDash(overview.revenueGrowth), label = "Rast prihoda", showDelta = true, delta = overview.revenueGrowthDelta, deltaSuffix = suffix) }
        add { m ->
            AnalyticsValueCard(
                modifier = m,
                value = "${overview.atRiskCustomers}",
                caption = "30+ dana bez narudžbe",
                label = "Kupci u riziku",
                onClick = component::openAtRiskCustomers
            )
        }
        if (overview.hasTarget) {
            add { m -> AnalyticsValueCard(modifier = m, value = formatPercentOrDash(overview.goalAchievement), label = "Ostvarenje cilja", showDelta = true, delta = overview.goalAchievementDelta, deltaSuffix = suffix) }
        }
    }
    AnalyticsGrid(phone = 2, wide = 4, items = cards)
}

@Composable
private fun TrendsTab(component: AnalyticsComponent) {
    val trends by component.trends.collectAsState()

    AnalyticsGrid(
        phone = 1,
        wide = 2,
        equalHeight = false,
        items = listOf<@Composable (Modifier) -> Unit>(
            { m -> TrendChartCard(modifier = m, title = "Prihod", series = trends.revenue, showFill = true, integerValues = false, yLabel = ::formatMoney) },
            { m -> TrendChartCard(modifier = m, title = "Narudžbe", series = trends.orders) },
            { m ->
                TrendChartCard(
                    modifier = m,
                    title = "Aktivni kupci",
                    series = trends.activeCustomers,
                    info = "Broj različitih kupaca koji su naručili u svakom periodu na grafikonu. Kupac koji je naručio u više perioda broji se u svakom od njih."
                )
            },
            { m -> TrendChartCard(modifier = m, title = "Novi kupci", series = trends.newCustomers) },
        )
    )
}

@Composable
private fun TrendChartCard(
    modifier: Modifier,
    title: String,
    series: AnalyticsTrendSeries,
    showFill: Boolean = false,
    integerValues: Boolean = true,
    yLabel: (Double) -> String = { formatNumber(it) },
    info: String? = null,
) {
    var showInfo by remember { mutableStateOf(false) }
    WhiteCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KarikaText(text = title, color = KarikaUiColors.Ink, textSize = 15.sp, fontWeight = FontWeight.W700)
            if (info != null) {
                XSpacer8()
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .border(width = 1.dp, color = KarikaUiColors.Subtle, shape = CircleShape)
                        .onClick { showInfo = !showInfo },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(text = "i", color = KarikaUiColors.Muted, textSize = 10.sp, fontWeight = FontWeight.W700)
                }
            }
        }
        if (info != null && showInfo) {
            KarikaText(text = info, color = KarikaUiColors.Muted, textSize = 11.sp, fontWeight = FontWeight.W400, lineHeight = 14.sp)
        }
        YSpacer8()
        AnalyticsLineChart(
            points = series.points,
            hasData = series.hasData,
            showFill = showFill,
            integerValues = integerValues,
            yLabel = yLabel
        )
    }
}

private enum class RepColumn(
    val title: String,
    val width: Dp,
    val sortValue: (SalesRepPerformance) -> Double?,
    val text: (SalesRepPerformance) -> String,
) {
    Revenue("Prihod", 120.dp, { it.revenue }, { formatMoneyOrDash(it.revenue) }),
    Orders("Narudžbe", 95.dp, { it.orders?.toDouble() }, { formatCountOrDash(it.orders) }),
    Aov("AOV", 115.dp, { it.avgOrderValue }, { formatMoneyOrDash(it.avgOrderValue) }),
    ActiveCustomers("Aktivni kupci", 115.dp, { it.activeCustomers?.toDouble() }, { formatCountOrDash(it.activeCustomers) }),
    NewCustomers("Novi kupci", 100.dp, { it.newCustomers?.toDouble() }, { formatCountOrDash(it.newCustomers) }),
    Growth("Rast prihoda", 115.dp, { it.growthPercent }, { formatPercentOrDash(it.growthPercent) }),
    Goal("Ostvarenje cilja", 135.dp, { it.goalPercent }, { formatPercentOrDash(it.goalPercent) }),
    RevenuePerCustomer("Prihod po kupcu", 135.dp, { it.revenuePerCustomer }, { formatMoneyOrDash(it.revenuePerCustomer) }),
    AssignedCustomers("Dodijeljeni kupci", 140.dp, { it.assignedCustomers?.toDouble() }, { formatCountOrDash(it.assignedCustomers) }),
    RepeatRate("Stopa ponovljene kupovine", 195.dp, { it.repeatPurchaseRate }, { formatPercentOrDash(it.repeatPurchaseRate) }),
}

private val RepNameColumnWidth = 160.dp
private val RepHeaderHeight = 40.dp
private val RepRowHeight = 56.dp

@Composable
private fun RepsTab(component: AnalyticsComponent) {
    val data by component.reps.collectAsState()
    val loader by component.loader.collectAsState()
    var sortColumn by remember { mutableStateOf(RepColumn.Revenue) }
    var descending by remember { mutableStateOf(true) }

    val reps = remember(data.reps, sortColumn, descending) {
        val (withValue, withoutValue) = data.reps.partition { sortColumn.sortValue(it) != null }
        val sorted = withValue.sortedBy { sortColumn.sortValue(it) }
        (if (descending) sorted.reversed() else sorted) + withoutValue
    }

    KarikaText(text = "Učinak komercijalista", color = KarikaUiColors.Ink, textSize = 16.sp, fontWeight = FontWeight.W700)

    if (reps.isEmpty()) {
        AnalyticsEmptyState(loader)
    } else {
        RepsTable(
            reps = reps,
            sortColumn = sortColumn,
            descending = descending,
            onSort = { column ->
                if (column == sortColumn) {
                    descending = !descending
                } else {
                    sortColumn = column
                    descending = true
                }
            }
        )
    }

    KarikaText(
        text = "Prihod prati trenutnu dodjelu kupca. Promjena dodjele prenosi cijelu historiju narudžbi na novog komercijalistu.",
        color = KarikaUiColors.Muted,
        textSize = 11.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 14.sp
    )
}

/** Web-like table: sticky "Komercijalista" column, the rest scrolls horizontally. */
@Composable
private fun RepsTable(
    reps: List<SalesRepPerformance>,
    sortColumn: RepColumn,
    descending: Boolean,
    onSort: (RepColumn) -> Unit,
) {
    val lineColor = KarikaUiColors.Line
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ValueCardShape)
            .background(KarikaColors.White)
            .border(width = 1.dp, color = KarikaUiColors.Border, shape = ValueCardShape)
    ) {
        Column(
            modifier = Modifier
                .width(RepNameColumnWidth)
                .drawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            RepHeaderCell(title = "Komercijalista", width = RepNameColumnWidth, alignEnd = false)
            reps.forEachIndexed { index, rep ->
                if (index > 0) RepRowDivider(RepNameColumnWidth)
                RepNameCell(rep)
            }
        }
        Column(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
            Row {
                RepColumn.entries.forEach { column ->
                    RepHeaderCell(
                        title = column.title,
                        width = column.width,
                        sortArrow = if (column == sortColumn) (if (descending) "↓" else "↑") else null,
                        onClick = { onSort(column) }
                    )
                }
            }
            val rowWidth = RepColumn.entries.fold(0.dp) { acc, column -> acc + column.width }
            reps.forEachIndexed { index, rep ->
                if (index > 0) RepRowDivider(rowWidth)
                Row {
                    RepColumn.entries.forEach { column ->
                        Box(
                            modifier = Modifier.width(column.width).height(RepRowHeight).padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            KarikaText(
                                text = column.text(rep),
                                color = KarikaUiColors.Ink,
                                textSize = 13.sp,
                                fontWeight = if (column == RepColumn.Revenue) FontWeight.W600 else FontWeight.W400,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepHeaderCell(
    title: String,
    width: Dp,
    alignEnd: Boolean = true,
    sortArrow: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .width(width)
            .height(RepHeaderHeight)
            .background(KarikaUiColors.Page)
            .then(if (onClick != null) Modifier.onClick(callback = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(text = title, color = KarikaUiColors.Muted, textSize = 12.sp, fontWeight = FontWeight.W600, maxLines = 1)
        if (sortArrow != null) {
            Spacer(modifier = Modifier.width(4.dp))
            KarikaText(text = sortArrow, color = KarikaUiColors.Ink, textSize = 12.sp, fontWeight = FontWeight.W700)
        }
    }
}

@Composable
private fun RepNameCell(rep: SalesRepPerformance) {
    Column(
        modifier = Modifier.width(RepNameColumnWidth).height(RepRowHeight).padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        KarikaText(text = rep.name, color = KarikaUiColors.Ink, textSize = 13.sp, fontWeight = FontWeight.W700, maxLines = 1)
        if (rep.suspended) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaUiColors.Field)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                KarikaText(text = "Suspendovan", color = KarikaUiColors.Muted, textSize = 10.sp, fontWeight = FontWeight.W600)
            }
        }
    }
}

@Composable
private fun RepRowDivider(width: Dp) {
    Box(modifier = Modifier.width(width).height(1.dp).background(KarikaUiColors.Line))
}

@Composable
private fun CustomersTab(component: AnalyticsComponent) {
    val customers by component.customers.collectAsState()

    SectionCaption("U PERIODU")
    AnalyticsGrid(
        phone = 2,
        wide = 4,
        items = listOf<@Composable (Modifier) -> Unit>(
            { m ->
                AnalyticsValueCard(
                    modifier = m,
                    value = formatCountOrDash(customers.assignedCustomers),
                    label = "Dodijeljeni kupci",
                    note = "Kupac može biti dodijeljen više komercijalista, pa se portfelji mogu preklapati."
                )
            },
            { m -> AnalyticsValueCard(modifier = m, value = formatCountOrDash(customers.activeCustomers), label = "Aktivni kupci") },
            { m -> AnalyticsValueCard(modifier = m, value = formatCountOrDash(customers.newCustomers), label = "Novi kupci") },
            { m -> AnalyticsValueCard(modifier = m, value = formatCountOrDash(customers.repeatCustomers), label = "Ponavljajući kupci (u periodu)") },
            { m -> AnalyticsValueCard(modifier = m, value = formatPercentOrDash(customers.repeatPurchaseRate), label = "Stopa ponovljene kupovine (u periodu)") },
            { m -> AnalyticsValueCard(modifier = m, value = formatMoneyOrDash(customers.revenuePerCustomer), label = "Prihod po kupcu") },
            { m ->
                AnalyticsValueCard(
                    modifier = m,
                    value = customers.avgOrdersPerCustomer?.let(::formatNumber) ?: ANALYTICS_NO_VALUE,
                    label = "Prosječan broj narudžbi po kupcu"
                )
            },
        )
    )

    SectionCaption("UKUPNO")
    AnalyticsGrid(
        phone = 2,
        wide = 4,
        items = listOf<@Composable (Modifier) -> Unit>(
            { m -> AnalyticsValueCard(modifier = m, value = formatCountOrDash(customers.customersWith2PlusOrders), label = "Kupci sa 2+ narudžbe ukupno") },
            { m -> AnalyticsValueCard(modifier = m, value = formatCountOrDash(customers.customersWithoutOrders), label = "Kupci bez narudžbe") },
        )
    )
}
