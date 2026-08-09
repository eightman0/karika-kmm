package karika.distribucija.ba.salesrep.ui.customers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCustomerBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer

class CustomersAdapter(
    private val onClick: (OperationalCustomer) -> Unit,
    private val onOrderClick: (OperationalCustomer) -> Unit
) : ListAdapter<OperationalCustomer, CustomersAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: OperationalCustomer) {
            binding.textCompany.text = customer.company ?: customer.fullName
            binding.textEmail.text = customer.email.orEmpty()
            binding.textAssigned.text = if (customer.assignedEmployees.isNotEmpty()) {
                "Zadužen: " + customer.assignedEmployees.joinToString(", ") { it.displayName ?: "—" }
            } else {
                ""
            }
            binding.textStatus.text = customer.statusLabel()
            binding.textStatus.background.setTint(statusColor(customer.partnershipStatus))
            binding.buttonOrder.visibility = if (customer.isActive) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onClick(customer) }
            binding.buttonOrder.setOnClickListener { onOrderClick(customer) }
        }

        private fun statusColor(status: String): Int {
            val colorRes = when (status) {
                "active" -> R.color.status_approved
                "pending" -> R.color.status_pending
                "rejected" -> R.color.status_rejected
                "revoked" -> R.color.status_cancelled
                else -> R.color.status_default
            }
            return binding.root.context.getColor(colorRes)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OperationalCustomer>() {
            override fun areItemsTheSame(oldItem: OperationalCustomer, newItem: OperationalCustomer) =
                oldItem.customerId == newItem.customerId

            override fun areContentsTheSame(oldItem: OperationalCustomer, newItem: OperationalCustomer) =
                oldItem == newItem
        }
    }
}
