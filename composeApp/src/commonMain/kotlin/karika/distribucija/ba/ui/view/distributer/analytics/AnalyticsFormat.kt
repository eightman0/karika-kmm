package karika.distribucija.ba.ui.view.distributer.analytics

import kotlin.math.round

/**
 * Formats a plain (non-money) number - percentages, counts, averages - trimming to at most
 * two decimals and dropping trailing zeros, e.g. 160.0 -> "160", 18.5 -> "18.5", 124.27 -> "124.27".
 */
fun formatNumber(value: Double): String {
    val cents = round(value * 100).toLong()
    val whole = cents / 100
    val frac = kotlin.math.abs(cents % 100)
    return if (frac == 0L) {
        "$whole"
    } else {
        "$whole.${frac.toString().padStart(2, '0').trimEnd('0')}"
    }
}
