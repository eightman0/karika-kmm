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
import karika.distribucija.ba.salesrep.databinding.ItemInternalMessageBinding
import karika.distribucija.ba.salesrep.model.StaffThreadMessage

/** Mirrors composeApp's InternalMessageBubble/InternalNewMessageBubble (identical in both
 * SalesInternalConversationView.kt and SalesInternalNewMessageView.kt). */
class InternalMessageAdapter(
    private val counterpartName: () -> String
) : ListAdapter<StaffThreadMessage, InternalMessageAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemInternalMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInternalMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: StaffThreadMessage) {
            val context = binding.root.context
            val isMine = message.isMine

            binding.textSenderLabel.text = if (isMine) context.getString(R.string.internal_message_me_label) else counterpartName()
            binding.textSenderLabel.setTextColor(
                context.getColor(if (isMine) R.color.karika_blue else R.color.karika_primary)
            )

            binding.textMessageBubble.text = message.message
            binding.textMessageBubble.setBackgroundResource(
                if (isMine) R.drawable.bg_bubble_mine else R.drawable.bg_bubble_other
            )

            binding.textMessageTime.text = message.formattedTime()

            val gravity = if (isMine) Gravity.END else Gravity.START
            (binding.bubbleColumn.layoutParams as ConstraintLayout.LayoutParams).horizontalBias = if (isMine) 1f else 0f
            (binding.textSenderLabel.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.textMessageBubble.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.textMessageTime.layoutParams as LinearLayout.LayoutParams).gravity = gravity
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StaffThreadMessage>() {
            override fun areItemsTheSame(oldItem: StaffThreadMessage, newItem: StaffThreadMessage) =
                oldItem.messageId == newItem.messageId

            override fun areContentsTheSame(oldItem: StaffThreadMessage, newItem: StaffThreadMessage) =
                oldItem == newItem
        }
    }
}
