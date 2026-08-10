package karika.distribucija.ba.salesrep.ui.messages

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCustomerMessageBinding
import karika.distribucija.ba.salesrep.model.Message

/** Mirrors composeApp's CustomerMessageBubble/CustomerNewMessageBubble - shared by customer and
 * admin messaging (both bind against the same Message model). [formatTimestamp] lets each screen
 * replicate its own Compose counterpart's timestamp quirk: the conversation screen shows the raw
 * unformatted date (a dead `formatTime()` helper in that Compose file is never actually called),
 * while the new-message screen does call it and shows HH:mm - so callers pass either
 * `{ it }` (raw) or a real HH:mm formatter to match. */
class CustomerMessageAdapter(
    private val counterpartName: () -> String,
    private val formatTimestamp: (String?) -> String
) : ListAdapter<Message, CustomerMessageAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCustomerMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val context = binding.root.context
            val isMine = message.isVendor()

            binding.textSenderLabel.text = if (isMine) context.getString(R.string.customer_message_me_label) else counterpartName()
            binding.textSenderLabel.setTextColor(
                context.getColor(if (isMine) R.color.karika_blue else R.color.karika_primary)
            )

            binding.textMessageBubble.text = message.message()
            binding.textMessageBubble.setBackgroundResource(
                if (isMine) R.drawable.bg_bubble_mine else R.drawable.bg_bubble_other
            )

            binding.textMessageTime.text = formatTimestamp(message.date())

            val gravity = if (isMine) Gravity.END else Gravity.START
            (binding.bubbleColumn.layoutParams as ConstraintLayout.LayoutParams).horizontalBias = if (isMine) 1f else 0f
            (binding.textSenderLabel.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.textMessageBubble.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.textMessageTime.layoutParams as LinearLayout.LayoutParams).gravity = gravity
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem == newItem
        }
    }
}
