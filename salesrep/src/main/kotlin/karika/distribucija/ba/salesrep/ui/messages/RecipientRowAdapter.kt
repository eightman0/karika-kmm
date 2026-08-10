package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemRecipientRowBinding
import karika.distribucija.ba.salesrep.model.StaffRecipient

class RecipientRowAdapter(
    private val onClick: (StaffRecipient) -> Unit
) : RecyclerView.Adapter<RecipientRowAdapter.ViewHolder>() {

    private var items: List<StaffRecipient> = emptyList()

    fun submitList(newItems: List<StaffRecipient>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemRecipientRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], isLast = position == items.lastIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemRecipientRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipient: StaffRecipient, isLast: Boolean) {
            binding.textRecipientName.text = recipient.name
            binding.textRecipientRole.text = recipient.displayRole()
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.contentRow.setOnClickListener { onClick(recipient) }
        }
    }
}
