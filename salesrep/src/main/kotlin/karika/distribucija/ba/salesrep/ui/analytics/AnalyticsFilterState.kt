package karika.distribucija.ba.salesrep.ui.analytics

import androidx.lifecycle.MutableLiveData
import karika.distribucija.ba.salesrep.util.toApiDate
import karika.distribucija.ba.salesrep.util.toDisplayDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Filters shared by every Analitika screen (Pregled/Trendovi/Komercijalisti/Analitika
 * kupaca/Kupci u riziku/Proizvodi i kategorije) and the dedicated Filteri screen, mirroring
 * composeApp's AnalyticsFilterState.kt. A plain singleton (like [karika.distribucija.ba.salesrep.session.CurrentUser])
 * rather than a shared ViewModel, since this module's nav graph is flat.
 *
 * Editing a field only updates these values - it does not trigger a refetch. [apply] bumps
 * [applyTrigger], which each analytics screen observes with `observe(viewLifecycleOwner)`: since
 * a Fragment's view (and thus its LiveData observer) is recreated when the user comes back from
 * the Filteri screen, LiveData's "redeliver last value to a new observer" behavior alone is enough
 * to refetch exactly once per filter visit, with no extra plumbing.
 */
object AnalyticsFilterState {
    // Web default: the last 30 days ending today, inclusive (today-29 .. today), where "today" is
    // the Europe/Sarajevo calendar day. Stored as UTC-midnight millis - the same encoding the
    // MaterialDatePicker selections use - so toApiDate()/toDisplayDate() (UTC) yield that exact day
    // even between 00:00 and 02:00 local time.
    private val defaultToday = Clock.System.todayIn(TimeZone.of("Europe/Sarajevo"))
    private val defaultDateFrom =
        defaultToday.minus(29, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    private val defaultDateTo = defaultToday.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    val dateFromMillis = MutableLiveData(defaultDateFrom)
    val dateToMillis = MutableLiveData(defaultDateTo)

    val groupingOptions = listOf("Dan", "Sedmica", "Mjesec", "Godina")
    val grouping = MutableLiveData(groupingOptions.first())

    val comparisonOptions = listOf("Prethodni period", "Prethodni mjesec", "Prethodna godina")
    val comparison = MutableLiveData(comparisonOptions.first())

    val repQuery = MutableLiveData("")
    val customerQuery = MutableLiveData("")
    val productQuery = MutableLiveData("")
    val categoryQuery = MutableLiveData("")

    val applyTrigger = MutableLiveData(0)

    fun apply() {
        applyTrigger.value = (applyTrigger.value ?: 0) + 1
    }

    fun dateFromApi(): String = (dateFromMillis.value ?: defaultDateFrom).toApiDate()
    fun dateToApi(): String = (dateToMillis.value ?: defaultDateTo).toApiDate()

    fun groupByApi(): String = when (grouping.value) {
        "Sedmica" -> "week"
        "Mjesec" -> "month"
        "Godina" -> "year"
        else -> "day"
    }

    fun comparisonBasisApi(): String = when (comparison.value) {
        "Prethodni mjesec" -> "previous_month"
        "Prethodna godina" -> "previous_year"
        else -> "previous_period"
    }

    /** Summary shown in the small filter-bar row on the Pregled/Trendovi/Komercijalisti/Analitika
     * kupaca screens, e.g. "01.09.2026. – 29.09.2026. · Dan · Prethodni period". */
    fun summaryText(): String {
        val from = (dateFromMillis.value ?: defaultDateFrom).toDisplayDate()
        val to = (dateToMillis.value ?: defaultDateTo).toDisplayDate()
        return "$from – $to · ${grouping.value} · ${comparison.value}"
    }
}
