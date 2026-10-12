package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemRecipientRowBinding
import karika.distribucija.ba.salesrep.model.ChatRecipient

class RecipientRowAdapter(
    private val onClick: (ChatRecipient) -> Unit
) : RecyclerView.Adapter<RecipientRowAdapter.ViewHolder>() {

    private var items: List<ChatRecipient> = emptyList()

    fun submitList(newItems: List<ChatRecipient>) {
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

        fun bind(recipient: ChatRecipient, isLast: Boolean) {
            binding.textRecipientName.text = recipient.name ?: "-"
            binding.textRecipientInitials.text = recipient.name.messageInitials()
            binding.textRecipientRole.text = binding.root.context.staffRoleLabel(recipient.counterpartType)
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.contentRow.setOnClickListener { onClick(recipient) }
        }
    }
}
