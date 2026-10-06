package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.EmployeeLocationHistory
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.XSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.employees.EmployeeAvatar
import karika.distribucija.ba.ui.view.distributer.employees.EmployeesInfoState
import karika.distribucija.ba.ui.view.distributer.employees.EmployeesPill
import karika.distribucija.ba.ui.view.distributer.employees.RoleBadge
import karika.distribucija.ba.ui.view.distributer.employees.StatusBadge
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_location
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@Composable
fun EmployeeLocationsView(component: EmployeeLocationsComponent) {
    val period by component.period.collectAsState()
    val history by component.history.collectAsState()
    val error by component.error.collectAsState()
    val loader by component.loader.collectAsState()
    val employee = component.employee
    val uriHandler = LocalUriHandler.current

    // Newest first for the timeline; the route sketch keeps chronological order.
    val timeline = remember(history) {
        history?.items.orEmpty().asReversed().map { it to it.effectiveAt.toLocal() }
    }
    val multiDay = period != LocationPeriod.Today && period != LocationPeriod.Yesterday
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var focus by remember(history) { mutableStateOf<EmployeeLocationPoint?>(null) }
    // Map opens on the latest pin only; the full trace is opt-in and kept across period changes.
    var showRoute by remember { mutableStateOf(false) }
    // back, header, periods, summary, [sampled banner], map
    val mapItemIndex = if (history?.sampled == true) 5 else 4

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.onClick { component.dashBack() }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(Res.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = KarikaColors.Blue
                )
                XSpacer8()
                KarikaText(text = "Komercijalisti", color = KarikaColors.Blue, textSize = 14.sp, fontWeight = FontWeight.W600)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmployeeAvatar(employee, 52.dp)
                XSpacer8()
                Column(
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KarikaText(text = employee.fullName, color = KarikaColors.Gray2, textSize = 18.sp, fontWeight = FontWeight.W700, maxLines = 1)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RoleBadge(employee.role)
                        StatusBadge(employee.status)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocationPeriod.entries.forEach {
                    EmployeesPill(text = it.label, selected = period == it, onClick = { component.selectPeriod(it) })
                }
            }
        }

        val data = history
        when {
            error != null -> item {
                EmployeesInfoState(title = "Historija nije dostupna", text = error.orEmpty())
            }

            data == null -> Unit

            !data.hasAny -> item {
                EmployeesInfoState(
                    title = "Bez lokacija",
                    text = "Ovaj komercijalista još nije poslao nijednu lokaciju."
                )
            }

            data.totalCount == 0 || data.items.isEmpty() -> item {
                EmployeesInfoState(
                    title = "Nema lokacija u periodu",
                    text = "Postoje ranije lokacije, ali nijedna za odabrani period. Probajte duži period."
                )
            }

            else -> {
                item { SummaryRow(data) }

                if (data.sampled) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(KarikaColors.Yellow1)
                                .padding(12.dp)
                        ) {
                            KarikaText(
                                text = "Prikazan je uzorak od ${data.items.size} tačaka od ukupno ${data.totalCount}. Prva i zadnja tačka su uvijek uključene.",
                                color = KarikaColors.Yellow2,
                                textSize = 12.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }

                item {
                    RouteCard(
                        points = data.items,
                        focus = focus,
                        showRoute = showRoute,
                        onToggleRoute = { showRoute = !showRoute },
                        onOpenLast = {
                            (data.lastReported ?: data.items.last()).let {
                                runCatching { uriHandler.openUri(it.mapsUrl()) }
                            }
                        }
                    )
                }

                item {
                    KarikaText(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Kretanje",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }

                itemsIndexed(items = timeline, key = { _, it -> it.first.locationId }) { index, (point, local) ->
                    val previous = timeline.getOrNull(index - 1)?.second
                    val showDayHeader = multiDay && (previous == null || previous.date != local?.date)
                    TimelineRow(
                        point = point,
                        local = local,
                        dayHeader = if (showDayHeader) local?.formatDate() else null,
                        isFirst = index == 0,
                        isLast = index == timeline.lastIndex,
                        selected = focus?.locationId == point.locationId,
                        onClick = {
                            focus = point
                            scope.launch { listState.animateScrollToItem(mapItemIndex) }
                        }
                    )
                }
            }
        }

        if (data == null && error == null && !loader) {
            item { EmployeesInfoState(title = "Učitavanje", text = "Dohvatam lokacije…") }
        }
    }

    LaunchedEffect(Unit) {
        component.load()
    }
}

@Composable
private fun SummaryRow(data: EmployeeLocationHistory) {
    val last = data.lastReported ?: data.items.lastOrNull()
    val lastLocal = last?.effectiveAt?.toLocal()
    val devices = data.items.map { it.deviceId }.distinct().size

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatTile(Modifier.weight(1f), "Tačaka", data.totalCount.toString())
        StatTile(
            Modifier.weight(1f),
            "Zadnja",
            lastLocal?.let { "${it.formatTime()}\n${it.formatDate()}" } ?: "—"
        )
        StatTile(Modifier.weight(1f), if (devices == 1) "Uređaj" else "Uređaja", devices.toString())
    }
}

@Composable
private fun StatTile(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(KarikaColors.Gray14)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KarikaText(text = label, color = KarikaColors.Gray13, textSize = 11.sp, fontWeight = FontWeight.W500)
        KarikaText(text = value, color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W700)
    }
}

@Composable
private fun RouteCard(
    points: List<EmployeeLocationPoint>,
    focus: EmployeeLocationPoint?,
    showRoute: Boolean,
    onToggleRoute: () -> Unit,
    onOpenLast: () -> Unit
) {
    val hasRoute = points.size > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, KarikaColors.Border, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        Box {
            EmployeeRouteMap(
                points = points,
                focus = focus,
                showRoute = showRoute && hasRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
            if (hasRoute) {
                RouteToggle(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    checked = showRoute,
                    onClick = onToggleRoute
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showRoute && hasRoute) {
                LegendDot(KarikaColors.Green5)
                KarikaText(text = "Početak", color = KarikaColors.Gray13, textSize = 12.sp, fontWeight = FontWeight.W500)
                Box(Modifier.width(12.dp))
            }
            LegendDot(KarikaColors.Primary)
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Zadnja lokacija",
                color = KarikaColors.Gray13,
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
            KarikaText(
                modifier = Modifier.onClick(callback = onOpenLast),
                text = "Otvori u Mapama",
                color = KarikaColors.Blue,
                textSize = 13.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

/** Floating checkbox chip over the map that turns the full movement line on and off. */
@Composable
private fun RouteToggle(modifier: Modifier, checked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) KarikaColors.Primary else KarikaColors.White)
            .onClick(callback = onClick)
            .padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) KarikaColors.White else KarikaColors.Transparent)
                .border(1.5.dp, if (checked) KarikaColors.White else KarikaColors.Gray8, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                KarikaText(text = "✓", color = KarikaColors.Primary, textSize = 13.sp, fontWeight = FontWeight.W700)
            }
        }
        XSpacer8()
        KarikaText(
            text = "Linija kretanja",
            color = if (checked) KarikaColors.White else KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun TimelineRow(
    point: EmployeeLocationPoint,
    local: LocalDateTime?,
    dayHeader: String?,
    isFirst: Boolean,
    isLast: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column {
        if (dayHeader != null) {
            KarikaText(
                modifier = Modifier.padding(top = if (isFirst) 0.dp else 8.dp, bottom = 8.dp),
                text = dayHeader,
                color = KarikaColors.Gray13,
                textSize = 12.sp,
                fontWeight = FontWeight.W700
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) KarikaColors.Blue3_10 else KarikaColors.Transparent)
                .onClick(callback = onClick)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.width(48.dp),
                text = local?.formatTime() ?: point.effectiveAt,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isFirst -> KarikaColors.Primary
                            isLast -> KarikaColors.Green5
                            else -> KarikaColors.Blue
                        }
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KarikaText(
                    text = "${point.latitude.format(5)}, ${point.longitude.format(5)}",
                    color = KarikaColors.Gray18,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
                val details = listOfNotNull(
                    point.accuracy?.let { "±${it.roundToInt()} m" },
                    point.speed?.takeIf { it > 0 }?.let { "${(it * 3.6).roundToInt()} km/h" },
                    if (point.deviceTimestamp == null) "vrijeme prijema" else null
                )
                if (details.isNotEmpty()) {
                    KarikaText(
                        text = details.joinToString(" · "),
                        color = KarikaColors.Gray13,
                        textSize = 11.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            }
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = vectorResource(Res.drawable.ic_location),
                contentDescription = null,
                tint = KarikaColors.Gray8
            )
        }
    }
}

private fun EmployeeLocationPoint.mapsUrl() =
    "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"

/** API timestamps are UTC `Y-m-d H:i:s` (not ISO-8601). */
@OptIn(ExperimentalTime::class)
private fun String.toLocal(): LocalDateTime? = runCatching {
    LocalDateTime.parse(trim().replace(' ', 'T'))
        .toInstant(TimeZone.UTC)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}.getOrNull()

private fun Int.pad2() = toString().padStart(2, '0')

private fun LocalDateTime.formatTime() = "${hour.pad2()}:${minute.pad2()}"

private fun LocalDateTime.formatDate() = "${day.pad2()}.${month.number.pad2()}.$year"

private fun Double.format(decimals: Int): String {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    val rounded = (this * factor).roundToInt() / factor
    val text = rounded.toString()
    val dot = text.indexOf('.')
    return if (dot < 0) text + "." + "0".repeat(decimals) else text.padEnd(dot + 1 + decimals, '0')
}
