package karika.distribucija.ba.salesrep.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderDetailBinding
import karika.distribucija.ba.salesrep.model.OnBehalfOrder

/**
 * Basic detail view assembled from the OnBehalfOrder already loaded by the orders list - no
 * extra network call yet. Full detail (line items, comments, PDF print) mirrors composeApp's
 * SalesOrderDetailComponent/View and needs DashRepository.getOrder() + VendorOrder/Comment
 * models, which are a follow-up phase.
 */
class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val order = OnBehalfOrder(
            orderId = args.getLong("orderId"),
            incrementId = args.getString("incrementId").orEmpty(),
            customerName = args.getString("customerName"),
            grandTotal = args.getFloat("grandTotal"),
            status = args.getString("status").orEmpty(),
            createdAt = args.getString("createdAt")
        )

        binding.textIncrementId.text = "#${order.incrementId}"
        binding.textStatus.text = order.statusLabel()
        binding.textStatus.background.setTint(statusColor(order.status))
        binding.textCustomer.text = order.displayName()
        binding.textDate.text = order.date()
        binding.textTotal.text = order.totalString()
    }

    private fun statusColor(status: String): Int {
        val colorRes = when (status) {
            "pending" -> R.color.status_pending
            "processing" -> R.color.status_processing
            "approved" -> R.color.status_approved
            "bill-sent" -> R.color.status_bill_sent
            "estimate-sent" -> R.color.status_estimate_sent
            "rejected" -> R.color.status_rejected
            "cancelled" -> R.color.status_cancelled
            else -> R.color.status_default
        }
        return requireContext().getColor(colorRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
