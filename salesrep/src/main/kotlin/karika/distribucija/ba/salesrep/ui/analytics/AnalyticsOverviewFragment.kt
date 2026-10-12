package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsOverviewBinding
import karika.distribucija.ba.salesrep.model.AnalyticsKpi
import karika.distribucija.ba.salesrep.model.AnalyticsOverview

/** "Pregled prodaje" tab of the Analitika feature - the web's KPI card grid (revenue, orders, AOV,
 * customers, products, growth, optional goal achievement, customers at risk) for the currently
 * selected [AnalyticsFilterState] range. */
class AnalyticsOverviewFragment : Fragment() {

    private var _binding: FragmentAnalyticsOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsOverviewViewModel by viewModels()

    private var cards: OverviewCards? = null
    private var goalCardShown = false

    private class OverviewCards(
        val revenue: AnalyticsValueCard,
        val orders: AnalyticsValueCard,
        val avgOrderValue: AnalyticsValueCard,
        val activeCustomers: AnalyticsValueCard,
        val newCustomers: AnalyticsValueCard,
        val distinctProducts: AnalyticsValueCard,
        val revenueGrowth: AnalyticsValueCard,
        val goalAchievement: AnalyticsValueCard,
        val atRisk: AnalyticsValueCard,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }
        binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()

        val grid = binding.gridCards
        val inflater = layoutInflater
        cards = OverviewCards(
            revenue = AnalyticsValueCard(inflater, grid, R.string.ka2_total_revenue),
            orders = AnalyticsValueCard(inflater, grid, R.string.ka2_orders),
            avgOrderValue = AnalyticsValueCard(inflater, grid, R.string.ka2_avg_order_value),
            activeCustomers = AnalyticsValueCard(inflater, grid, R.string.ka2_active_customers),
            newCustomers = AnalyticsValueCard(inflater, grid, R.string.ka2_new_customers),
            distinctProducts = AnalyticsValueCard(inflater, grid, R.string.ka2_distinct_products),
            revenueGrowth = AnalyticsValueCard(inflater, grid, R.string.ka2_revenue_growth),
            goalAchievement = AnalyticsValueCard(inflater, grid, R.string.ka2_goal_achievement),
            atRisk = AnalyticsValueCard(
                inflater, grid, R.string.ka2_at_risk_customers, valueNote = R.string.ka2_at_risk_note
            ),
        ).also { cards ->
            cards.atRisk.root.apply {
                isClickable = true
                isFocusable = true
                val ripple = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
                foreground = context.getDrawable(ripple.resourceId)
                setOnClickListener { findNavController().navigate(R.id.analyticsAtRiskFragment) }
            }
        }
        goalCardShown = false
        arrangeCards()

        // Refreshes the filter-bar summary and (re)fetches the overview. This single observer
        // also fires once on first subscribe with the LiveData's current value, so load() must
        // not be called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.overview.observe(viewLifecycleOwner) { overview -> render(overview) }
    }

    /** Web order; "Ostvarenje cilja" only exists when the API reports a target (has_target). */
    private fun arrangeCards() {
        val cards = cards ?: return
        val ordered = buildList {
            add(cards.revenue.root)
            add(cards.orders.root)
            add(cards.avgOrderValue.root)
            add(cards.activeCustomers.root)
            add(cards.newCustomers.root)
            add(cards.distinctProducts.root)
            add(cards.revenueGrowth.root)
            if (goalCardShown) add(cards.goalAchievement.root)
            add(cards.atRisk.root)
        }
        arrangeAnalyticsGrid(
            binding.gridCards,
            ordered,
            resources.getInteger(R.integer.ka2_value_card_columns)
        )
    }

    private fun render(overview: AnalyticsOverview) {
        val cards = cards ?: return
        val caption = AnalyticsValueCard.comparisonCaption(binding.root)

        fun AnalyticsValueCard.bind(kpi: AnalyticsKpi, format: (Double?) -> String) {
            setValue(format(kpi.value))
            setDelta(kpi.deltaPercent, caption)
        }

        cards.revenue.bind(overview.totalRevenue, AnalyticsFormatting::money)
        cards.orders.bind(overview.totalOrders, AnalyticsFormatting::count)
        cards.avgOrderValue.bind(overview.avgOrderValue, AnalyticsFormatting::money)
        cards.activeCustomers.bind(overview.activeCustomers, AnalyticsFormatting::count)
        cards.newCustomers.bind(overview.newCustomers, AnalyticsFormatting::count)
        cards.distinctProducts.bind(overview.distinctProducts, AnalyticsFormatting::count)
        cards.revenueGrowth.bind(overview.revenueGrowth, AnalyticsFormatting::percent)
        cards.goalAchievement.bind(overview.goalAchievement, AnalyticsFormatting::percent)
        cards.atRisk.setValue(AnalyticsFormatting.count(overview.atRiskCustomers))

        if (overview.hasTarget != goalCardShown) {
            goalCardShown = overview.hasTarget
            arrangeCards()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cards = null
        _binding = null
    }
}
