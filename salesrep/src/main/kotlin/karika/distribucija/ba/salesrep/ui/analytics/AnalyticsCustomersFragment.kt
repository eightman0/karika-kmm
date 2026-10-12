package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsCustomersBinding
import karika.distribucija.ba.salesrep.model.CustomerAnalytics

/** "Statistika kupaca" tab of the Analitika feature - the web's "Analitika kupaca" section: "U
 * PERIODU" cards (assigned/active/new/repeat customers, repeat-purchase rate, revenue and orders
 * per customer) and "UKUPNO" cards (customers with 2+ orders, customers without orders) for the
 * currently selected [AnalyticsFilterState] range. */
class AnalyticsCustomersFragment : Fragment() {

    private var _binding: FragmentAnalyticsCustomersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsCustomersViewModel by viewModels()

    private var cards: CustomerCards? = null

    private class CustomerCards(
        val assigned: AnalyticsValueCard,
        val active: AnalyticsValueCard,
        val new: AnalyticsValueCard,
        val repeat: AnalyticsValueCard,
        val repeatRate: AnalyticsValueCard,
        val revenuePerCustomer: AnalyticsValueCard,
        val avgOrders: AnalyticsValueCard,
        val twoPlusOrders: AnalyticsValueCard,
        val withoutOrders: AnalyticsValueCard,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }

        val inflater = layoutInflater
        val inPeriod = binding.gridInPeriod
        val total = binding.gridTotal
        val cards = CustomerCards(
            assigned = AnalyticsValueCard(
                inflater, inPeriod, R.string.ka2_assigned_customers,
                labelNote = R.string.ka2_assigned_customers_note
            ),
            active = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_active_customers),
            new = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_new_customers),
            repeat = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_repeat_customers),
            repeatRate = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_repeat_purchase_rate),
            revenuePerCustomer = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_revenue_per_customer),
            avgOrders = AnalyticsValueCard(inflater, inPeriod, R.string.ka2_avg_orders_per_customer),
            twoPlusOrders = AnalyticsValueCard(inflater, total, R.string.ka2_customers_2plus_total),
            withoutOrders = AnalyticsValueCard(inflater, total, R.string.ka2_customers_without_orders),
        )
        this.cards = cards
        val columns = resources.getInteger(R.integer.ka2_value_card_columns)
        arrangeAnalyticsGrid(
            inPeriod,
            listOf(
                cards.assigned.root, cards.active.root, cards.new.root, cards.repeat.root,
                cards.repeatRate.root, cards.revenuePerCustomer.root, cards.avgOrders.root,
            ),
            columns
        )
        arrangeAnalyticsGrid(total, listOf(cards.twoPlusOrders.root, cards.withoutOrders.root), columns)

        // Refreshes the filter-bar summary and (re)fetches the customer analytics. This single
        // observer also fires once on first subscribe with the LiveData's current value, so
        // load() must not be called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.customerAnalytics.observe(viewLifecycleOwner) { data -> render(data) }
    }

    private fun render(data: CustomerAnalytics) {
        val cards = cards ?: return
        cards.assigned.setValue(AnalyticsFormatting.count(data.assignedCustomers))
        cards.active.setValue(AnalyticsFormatting.count(data.activeCustomers))
        cards.new.setValue(AnalyticsFormatting.count(data.newCustomers))
        cards.repeat.setValue(AnalyticsFormatting.count(data.repeatCustomers))
        cards.repeatRate.setValue(AnalyticsFormatting.percent(data.repeatPurchaseRate))
        cards.revenuePerCustomer.setValue(AnalyticsFormatting.money(data.revenuePerCustomer))
        cards.avgOrders.setValue(AnalyticsFormatting.decimal(data.avgOrdersPerCustomer, 2))
        cards.twoPlusOrders.setValue(AnalyticsFormatting.count(data.customersWith2PlusOrders))
        cards.withoutOrders.setValue(AnalyticsFormatting.count(data.customersWithoutOrders))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cards = null
        _binding = null
    }
}
