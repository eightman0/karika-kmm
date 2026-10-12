package karika.distribucija.ba.salesrep.ui.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemAnalyticsValueCardBinding

/**
 * Web-style KPI card (item_analytics_value_card.xml) shared by Pregled prodaje and Statistika
 * kupaca: big bold value, label under it, optional right-aligned delta with a
 * "vs prethodni period" caption, and optional muted notes under the value / label.
 */
internal class AnalyticsValueCard(
    inflater: LayoutInflater,
    parent: ViewGroup,
    @StringRes label: Int,
    @StringRes valueNote: Int? = null,
    @StringRes labelNote: Int? = null,
) {
    private val binding = ItemAnalyticsValueCardBinding.inflate(inflater, parent, false)
    val root: View get() = binding.root

    init {
        binding.textLabel.setText(label)
        binding.textValue.text = AnalyticsFormatting.EMPTY_VALUE
        if (valueNote != null) {
            binding.textValueNote.setText(valueNote)
            binding.textValueNote.visibility = View.VISIBLE
        }
        if (labelNote != null) {
            binding.textLabelNote.setText(labelNote)
            binding.textLabelNote.visibility = View.VISIBLE
        }
    }

    fun setValue(text: String) {
        binding.textValue.text = text
    }

    /** Shows the delta column: "–" / "0%" muted, "+x%" green, "-x%" red. */
    fun setDelta(deltaPercent: Double?, comparisonCaption: String) {
        val (text, tone) = AnalyticsFormatting.delta(deltaPercent)
        val color = when (tone) {
            AnalyticsFormatting.DeltaTone.POSITIVE -> R.color.k_green
            AnalyticsFormatting.DeltaTone.NEGATIVE -> R.color.k_red
            AnalyticsFormatting.DeltaTone.NEUTRAL -> R.color.k_muted
        }
        binding.textDelta.text = text
        binding.textDelta.setTextColor(root.context.getColor(color))
        binding.textDeltaCaption.text = comparisonCaption
        binding.layoutDelta.visibility = View.VISIBLE
    }

    companion object {
        /** "vs prethodni period/mjesec/godina" for the currently applied comparison basis. */
        fun comparisonCaption(view: View): String = view.context.getString(
            when (AnalyticsFilterState.comparisonBasisApi()) {
                "previous_month" -> R.string.ka2_vs_previous_month
                "previous_year" -> R.string.ka2_vs_previous_year
                else -> R.string.ka2_vs_previous_period
            }
        )
    }
}

/**
 * Lays [cards] out in rows of [columns] equal-width cells (12dp gutters) inside the vertical
 * [container]; cards in a row stretch to the tallest one. Re-callable - it clears [container] first.
 */
internal fun arrangeAnalyticsGrid(container: LinearLayout, cards: List<View>, columns: Int) {
    container.removeAllViews()
    cards.forEach { (it.parent as? ViewGroup)?.removeView(it) }
    val gutter = container.resources.getDimensionPixelSize(R.dimen.ka2_grid_gutter)
    cards.chunked(columns.coerceAtLeast(1)).forEachIndexed { rowIndex, rowCards ->
        val row = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
        }
        for (column in 0 until columns) {
            val cell = rowCards.getOrNull(column) ?: View(container.context)
            row.addView(
                cell,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    if (column > 0) marginStart = gutter
                }
            )
        }
        container.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { if (rowIndex > 0) topMargin = gutter }
        )
    }
}
