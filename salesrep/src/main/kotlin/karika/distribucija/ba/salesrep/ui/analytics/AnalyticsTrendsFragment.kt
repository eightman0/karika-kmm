package karika.distribucija.ba.salesrep.ui.analytics

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.patrykandpatrick.vico.views.cartesian.CartesianChart
import com.patrykandpatrick.vico.views.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.views.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.views.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.views.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.views.cartesian.data.lineSeries
import com.patrykandpatrick.vico.views.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.views.common.Fill
import com.patrykandpatrick.vico.views.common.Insets
import com.patrykandpatrick.vico.views.common.component.LineComponent
import com.patrykandpatrick.vico.views.common.component.ShapeComponent
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.shape.CorneredShape
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsTrendsBinding
import karika.distribucija.ba.salesrep.databinding.ItemAnalyticsChartCardBinding
import karika.distribucija.ba.salesrep.model.AnalyticsTrendSeries
import karika.distribucija.ba.salesrep.model.AnalyticsTrends
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/** "Trendovi prodaje" tab of the Analitika feature - the web's four line-chart cards (Prihod,
 * Narudžbe, Aktivni kupci, Novi kupci), one point per bucket of `GET /analytics/trends` for the
 * shared [AnalyticsFilterState], with the web's empty state when a series has no data. */
class AnalyticsTrendsFragment : Fragment() {

    private var _binding: FragmentAnalyticsTrendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsTrendsViewModel by viewModels()

    private class ChartCard(
        val binding: ItemAnalyticsChartCardBinding,
        val money: Boolean,
        val modelProducer: CartesianChartModelProducer,
    )

    private var revenueCard: ChartCard? = null
    private var ordersCard: ChartCard? = null
    private var activeCustomersCard: ChartCard? = null
    private var newCustomersCard: ChartCard? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }
        binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()

        val revenue = createCard(R.string.ka2_chart_revenue, money = true)
        val orders = createCard(R.string.ka2_chart_orders, money = false)
        val activeCustomers = createCard(
            R.string.ka2_chart_active_customers, money = false, info = R.string.ka2_active_customers_info
        )
        val newCustomers = createCard(R.string.ka2_chart_new_customers, money = false)
        revenueCard = revenue
        ordersCard = orders
        activeCustomersCard = activeCustomers
        newCustomersCard = newCustomers
        arrangeAnalyticsGrid(
            binding.gridCharts,
            listOf(revenue, orders, activeCustomers, newCustomers).map { it.binding.root },
            resources.getInteger(R.integer.ka2_chart_card_columns)
        )

        // Refreshes the filter-bar summary and (re)fetches the trends. This single observer also
        // fires once on first subscribe with the LiveData's current value, so load() must not be
        // called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.trends.observe(viewLifecycleOwner) { trends -> renderTrends(trends) }
    }

    private fun createCard(@StringRes title: Int, money: Boolean, @StringRes info: Int? = null): ChartCard {
        val cardBinding = ItemAnalyticsChartCardBinding.inflate(layoutInflater, binding.gridCharts, false)
        cardBinding.textTitle.setText(title)
        if (info != null) {
            cardBinding.imageInfo.visibility = View.VISIBLE
            cardBinding.imageInfo.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(info)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
        val modelProducer = CartesianChartModelProducer()
        cardBinding.chart.chart = buildChart(money)
        cardBinding.chart.modelProducer = modelProducer
        return ChartCard(cardBinding, money, modelProducer)
    }

    /** Blue smooth line with small dots, light horizontal + vertical grid, muted axis labels; the
     * revenue chart also gets the web's light-blue area fill and "450,00 KM" y labels. */
    private fun buildChart(money: Boolean): CartesianChart {
        val context = requireContext()
        val blue = context.getColor(R.color.k_accent)
        val grid = LineComponent(Fill(context.getColor(R.color.k_line)), thicknessDp = 1f)
        val axisLine = LineComponent(Fill(context.getColor(R.color.k_border)), thicknessDp = 1f)
        val axisLabel = TextComponent(
            color = context.getColor(R.color.k_muted),
            textSizeSp = 10f,
            margins = Insets(horizontalDp = 4f, verticalDp = 2f),
        )
        val line = LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(blue)),
            stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2f),
            areaFill = if (money) {
                LineCartesianLayer.AreaFill.single(Fill(ColorUtils.setAlphaComponent(blue, 0x33)))
            } else {
                null
            },
            pointProvider = LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.Point(ShapeComponent(Fill(blue), CorneredShape.Pill), sizeDp = 6f)
            ),
            interpolator = LineCartesianLayer.Interpolator.cubic(),
        )
        val yFormatter = CartesianValueFormatter { _, value, _ ->
            if (money) AnalyticsFormatting.money(value) else AnalyticsFormatting.count(value)
        }
        val xFormatter = CartesianValueFormatter { measuringContext, value, _ ->
            measuringContext.model.extraStore.getOrNull(labelsKey)
                ?.getOrNull(value.roundToInt())
                ?.ifEmpty { " " } ?: " "
        }
        return CartesianChart(
            LineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(line),
                rangeProvider = NiceYRange,
            ),
            startAxis = VerticalAxis.start(
                label = axisLabel,
                valueFormatter = yFormatter,
                guideline = grid,
                itemPlacer = VerticalAxis.ItemPlacer.step({ extras -> extras.getOrNull(yStepKey) }),
            ),
            bottomAxis = HorizontalAxis.bottom(
                line = axisLine,
                label = axisLabel,
                valueFormatter = xFormatter,
                guideline = grid,
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                    spacing = { extras -> extras.getOrNull(xLabelSpacingKey) ?: 1 }
                ),
            ),
        )
    }

    private fun renderTrends(trends: AnalyticsTrends) {
        revenueCard?.render(trends.revenue, trends.labels)
        ordersCard?.render(trends.orders, trends.labels)
        activeCustomersCard?.render(trends.activeCustomers, trends.labels)
        newCustomersCard?.render(trends.newCustomers, trends.labels)
    }

    private fun ChartCard.render(series: AnalyticsTrendSeries, labels: List<String>) {
        val hasData = series.hasData && series.values.isNotEmpty()
        binding.layoutEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        binding.chart.visibility = if (hasData) View.VISIBLE else View.INVISIBLE
        if (!hasData) return

        val values = series.values
        val (step, maxY) = niceYScale(values.maxOrNull() ?: 0.0, integer = !money)
        binding.chart.doOnLayout { chartView ->
            // Skip x labels so they never overlap: ~56dp per "13. sep" label across the plot.
            val plotWidthDp = chartView.width / chartView.resources.displayMetrics.density - 72f
            val maxLabels = (plotWidthDp / 56f).toInt().coerceAtLeast(2)
            val labelSpacing = ceil(values.size / maxLabels.toDouble()).toInt().coerceAtLeast(1)
            viewLifecycleOwner.lifecycleScope.launch {
                modelProducer.runTransaction {
                    lineSeries { series(values.indices.toList(), values) }
                    extras { extraStore ->
                        extraStore[labelsKey] = labels
                        extraStore[yStepKey] = step
                        extraStore[yMaxKey] = maxY
                        extraStore[xLabelSpacingKey] = labelSpacing
                    }
                }
            }
        }
    }

    /** A 0-based y scale with a "nice" step (1/2/2.5/5 x 10^n, whole numbers for counts) and about
     * four gridline intervals. */
    private fun niceYScale(maxValue: Double, integer: Boolean): Pair<Double, Double> {
        if (maxValue <= 0.0) return 1.0 to 4.0
        val raw = maxValue / 4
        val magnitude = 10.0.pow(floor(log10(raw)))
        val multipliers = if (integer && magnitude < 10) listOf(1.0, 2.0, 5.0, 10.0) else listOf(1.0, 2.0, 2.5, 5.0, 10.0)
        var step = multipliers.map { it * magnitude }.first { it >= raw }
        if (integer) step = ceil(step).coerceAtLeast(1.0)
        val maxY = (ceil(maxValue / step) * step).coerceAtLeast(step)
        return step to maxY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        revenueCard = null
        ordersCard = null
        activeCustomersCard = null
        newCustomersCard = null
        _binding = null
    }

    /** y range 0..[yMaxKey] (set per data update alongside [yStepKey]). */
    private object NiceYRange : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 0.0

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
            extraStore.getOrNull(yMaxKey) ?: maxY.coerceAtLeast(1.0)
    }

    companion object {
        private val labelsKey = ExtraStore.Key<List<String>>()
        private val yStepKey = ExtraStore.Key<Double>()
        private val yMaxKey = ExtraStore.Key<Double>()
        private val xLabelSpacingKey = ExtraStore.Key<Int>()
    }
}
