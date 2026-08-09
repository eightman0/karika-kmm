package karika.distribucija.ba.salesrep.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemCustomerSearchBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer

/** Mirrors composeApp's SalesInviteCustomerView.kt DropdownMenuItem (single-line, name/company only). */
class CustomerSearchAdapter(
    private val onClick: (OperationalCustomer) -> Unit
) : RecyclerView.Adapter<CustomerSearchAdapter.ViewHolder>() {

    private var items: List<OperationalCustomer> = emptyList()

    fun submitList(newItems: List<OperationalCustomer>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCustomerSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: OperationalCustomer) {
            binding.root.text = customer.company ?: customer.fullName
            binding.root.setOnClickListener { onClick(customer) }
        }
    }
}
