package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.ui.view.distributer.orders.toDate
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
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
 * `Y-m-d` query params via [toApiDate]); [dateFrom]/[dateTo] are the `d.M.yyyy.` display strings
 * the filter fields already render and are kept in sync whenever the date pickers fire.
 *
 * Editing a field only updates these values - it does not call the API. [applyTrigger] is bumped
 * by [apply] when the user leaves the Filteri screen (back or "Primijeni"), which is what the
 * analytics screens actually watch to refetch, so the API is only called once per filter visit.
 */
@OptIn(ExperimentalTime::class)
class AnalyticsFilterState {
    val dateFromMillis = mutableStateOf(Clock.System.now().minus(29.days).toEpochMilliseconds())
    val dateToMillis = mutableStateOf(Clock.System.now().toEpochMilliseconds())
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
