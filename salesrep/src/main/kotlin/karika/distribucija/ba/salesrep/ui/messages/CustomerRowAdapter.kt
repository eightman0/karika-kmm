package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemCustomerRowBinding
import karika.distribucija.ba.salesrep.model.ChatRecipient

/** Mirrors composeApp's RecipientRow in SalesRecipientPicker, as used by
 * SalesCustomerNewMessageView.kt - the Chat V2 recipient's name, no subtitle. */
class CustomerRowAdapter(
    private val onClick: (ChatRecipient) -> Unit
) : RecyclerView.Adapter<CustomerRowAdapter.ViewHolder>() {

    private var items: List<ChatRecipient> = emptyList()

    fun submitList(newItems: List<ChatRecipient>) {
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

        fun bind(customer: ChatRecipient, isLast: Boolean) {
            binding.textCustomerCompany.text = customer.name ?: "-"
            binding.textCustomerFullname.visibility = View.GONE
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.contentRow.setOnClickListener { onClick(customer) }
        }
    }
}
