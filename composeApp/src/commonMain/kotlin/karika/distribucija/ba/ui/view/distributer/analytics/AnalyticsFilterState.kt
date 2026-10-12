package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.ui.view.distributer.orders.toDate
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Filters for the whole Analitika section (shared by the Pregled/Trendovi/Komercijalisti/Kupci
 * tabs and the dedicated Filteri screen). Lives on [karika.distribucija.ba.ui.common.state.KarikaStateHolder]
 * rather than on a single screen's component so it survives navigating to the Filteri screen and back
 * (each drawer/stack navigation recreates the screen's own component).
 *
 * [dateFromMillis]/[dateToMillis] are the source of truth (used to build the `dateFrom`/`dateTo`
 * `Y-m-d` query params via [toApiDate]). Like the date picker's selection, they are UTC midnight of
 * the chosen calendar day, so [toApiDate] (UTC) yields exactly that day; [dateFrom]/[dateTo] are the `d.M.yyyy.` display strings
 * the filter fields already render and are kept in sync whenever the date pickers fire.
 *
 * Editing a field only updates these values - it does not call the API. [applyTrigger] is bumped
 * by [apply] when the user leaves the Filteri screen (back or "Primijeni"), which is what the
 * analytics screens actually watch to refetch, so the API is only called once per filter visit.
 */
@OptIn(ExperimentalTime::class)
class AnalyticsFilterState {
    /** Web default: the last 30 days ending today (Europe/Sarajevo), both ends inclusive. */
    val dateToMillis = mutableStateOf(analyticsTodayUtcMillis())
    val dateFromMillis = mutableStateOf(dateToMillis.value - 29.days.inWholeMilliseconds)
    val dateFrom = mutableStateOf(dateFromMillis.value.toDate1())
    val dateTo = mutableStateOf(dateToMillis.value.toDate1())
    val groupingOptions = mutableStateOf(listOf("Dan", "Sedmica", "Mjesec", "Godina"))
    val grouping = mutableStateOf("Dan")
    val comparisonOptions = mutableStateOf(listOf("Prethodni period", "Prethodni mjesec", "Prethodna godina"))
    val comparison = mutableStateOf("Prethodni period")
    val repQuery = mutableStateOf("")
    val customerQuery = mutableStateOf("")
    val productQuery = mutableStateOf("")
    val categoryQuery = mutableStateOf("")

    val applyTrigger = mutableStateOf(0)

    fun apply() {
        applyTrigger.value++
    }
}

/** Today's calendar date in the business timezone (Europe/Sarajevo), as UTC midnight millis. */
@OptIn(ExperimentalTime::class)
private fun analyticsTodayUtcMillis(): Long {
    val zone = runCatching { TimeZone.of("Europe/Sarajevo") }.getOrDefault(TimeZone.currentSystemDefault())
    return Clock.System.now().toLocalDateTime(zone).date
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}

fun Long.toApiDate(): String = toDate()

fun String.toApiGroupBy(): String = when (this) {
    "Sedmica" -> "week"
    "Mjesec" -> "month"
    "Godina" -> "year"
    else -> "day"
}

fun String.toApiComparisonBasis(): String = when (this) {
    "Prethodni mjesec" -> "previous_month"
    "Prethodna godina" -> "previous_year"
    else -> "previous_period"
}
