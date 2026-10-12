package karika.distribucija.ba.salesrep.ui.analytics

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Number formatting shared by the web-parity Analitika screens (Pregled prodaje, Trendovi prodaje,
 * Statistika kupaca): "," decimals and, like [karikaPriceFormat] used for money everywhere in this
 * app, no thousands grouping. A null value (API sent nothing) renders as [EMPTY_VALUE].
 */
internal object AnalyticsFormatting {

    const val EMPTY_VALUE = "—"
    const val DELTA_UNAVAILABLE = "–"

    private val symbols = DecimalFormatSymbols(Locale.ROOT).apply {
        decimalSeparator = ','
        minusSign = '-'
    }

    private fun decimalFormat(maxFractionDigits: Int) =
        DecimalFormat("0", symbols).apply {
            isGroupingUsed = false
            maximumFractionDigits = maxFractionDigits
            roundingMode = RoundingMode.HALF_UP
        }

    /** "405,00 KM" */
    fun money(value: Double?): String = value?.let { "${karikaPriceFormat(it)} KM" } ?: EMPTY_VALUE

    /** Whole count, e.g. "1234". */
    fun count(value: Double?): String = value?.let { decimalFormat(0).format(it) } ?: EMPTY_VALUE

    fun count(value: Int?): String = count(value?.toDouble())

    /** Up to [maxFractionDigits] decimals, no trailing zeros: 2 -> "2", 1.5 -> "1,5", 1.257 -> "1,26". */
    fun decimal(value: Double?, maxFractionDigits: Int = 2): String =
        value?.let { decimalFormat(maxFractionDigits).format(it) } ?: EMPTY_VALUE

    /** "100%", "12,5%" */
    fun percent(value: Double?): String = value?.let { "${decimal(it, 1)}%" } ?: EMPTY_VALUE

    enum class DeltaTone { NEUTRAL, POSITIVE, NEGATIVE }

    /** "–" when unavailable, "0%" when unchanged, otherwise a signed "+12,5%" / "-3,2%". */
    fun delta(deltaPercent: Double?): Pair<String, DeltaTone> {
        if (deltaPercent == null) return DELTA_UNAVAILABLE to DeltaTone.NEUTRAL
        val magnitude = decimal(abs(deltaPercent), 1)
        return when {
            magnitude == "0" -> "0%" to DeltaTone.NEUTRAL
            deltaPercent > 0 -> "+$magnitude%" to DeltaTone.POSITIVE
            else -> "-$magnitude%" to DeltaTone.NEGATIVE
        }
    }
}
