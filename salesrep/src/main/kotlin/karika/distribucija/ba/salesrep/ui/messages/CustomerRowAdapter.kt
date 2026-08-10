package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemCustomerRowBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer

/** Mirrors composeApp's CustomerRow in SalesCustomerNewMessageView.kt. */
class CustomerRowAdapter(
    private val onClick: (OperationalCustomer) -> Unit
) : RecyclerView.Adapter<CustomerRowAdapter.ViewHolder>() {

    private var items: List<OperationalCustomer> = emptyList()

    fun submitList(newItems: List<OperationalCustomer>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], isLast = position == items.lastIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCustomerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: OperationalCustomer, isLast: Boolean) {
            binding.textCustomerCompany.text = customer.company ?: customer.fullName
            val hasCompany = !customer.company.isNullOrEmpty()
            binding.textCustomerFullname.visibility = if (hasCompany) View.VISIBLE else View.GONE
            if (hasCompany) binding.textCustomerFullname.text = customer.fullName
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.contentRow.setOnClickListener { onClick(customer) }
        }
    }
}
