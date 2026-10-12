package karika.distribucija.ba.ui.view.distributer.analytics

import karika.distribucija.ba.util.karikaPriceFormat
import kotlin.math.abs
import kotlin.math.round

/** Shown in place of a value the API did not return. */
const val ANALYTICS_NO_VALUE = "—"

/** Shown in place of a delta whose comparison is not available. */
const val ANALYTICS_NO_DELTA = "–"

private const val MINUS = "−"

/**
 * Formats a plain (non-money) number - percentages, counts, averages - the Bosnian way: at most
 * two decimals, comma separator, no trailing zeros, e.g. 160.0 -> "160", 18.5 -> "18,5",
 * 124.27 -> "124,27", -0.5 -> "-0,5".
 */
fun formatNumber(value: Double): String {
    val cents = round(abs(value) * 100).toLong()
    val whole = cents / 100
    val frac = cents % 100
    val sign = if (value < 0 && cents != 0L) "-" else ""
    return if (frac == 0L) {
        "$sign$whole"
    } else {
        "$sign$whole,${frac.toString().padStart(2, '0').trimEnd('0')}"
    }
}

/** "405,00 KM" */
fun formatMoney(value: Double): String =
    if (value < 0) "-${karikaPriceFormat(-value)} KM" else "${karikaPriceFormat(value)} KM"

fun formatMoneyOrDash(value: Double?): String = value?.let(::formatMoney) ?: ANALYTICS_NO_VALUE

fun formatCountOrDash(value: Int?): String = value?.toString() ?: ANALYTICS_NO_VALUE

/** "12,5%" / "−8%"; "—" when null. */
fun formatPercentOrDash(value: Double?): String = when {
    value == null -> ANALYTICS_NO_VALUE
    value < 0 && formatNumber(-value) != "0" -> "$MINUS${formatNumber(-value)}%"
    else -> "${formatNumber(abs(value))}%"
}

/** Delta like the web: "+12,5%", "−8%", "0%"; "–" when the comparison is not available. */
fun formatDelta(value: Double?): String {
    if (value == null) return ANALYTICS_NO_DELTA
    val text = formatNumber(abs(value))
    return when {
        text == "0" -> "0%"
        value > 0 -> "+$text%"
        else -> "$MINUS$text%"
    }
}
