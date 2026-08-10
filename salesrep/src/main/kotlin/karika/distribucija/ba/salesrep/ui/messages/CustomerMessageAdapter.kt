package karika.distribucija.ba.salesrep.ui.messages

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCustomerMessageBinding
import karika.distribucija.ba.salesrep.model.FileData
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import karika.distribucija.ba.salesrep.util.loadUrl
import kotlinx.serialization.json.Json

/** Mirrors composeApp's CustomerMessageBubble/CustomerNewMessageBubble - shared by customer and
 * admin messaging (both bind against the same Message model). [formatTimestamp] lets each screen
 * replicate its own Compose counterpart's timestamp quirk: the conversation screen shows the raw
 * unformatted date (a dead `formatTime()` helper in that Compose file is never actually called),
 * while the new-message screen does call it and shows HH:mm - so callers pass either
 * `{ it }` (raw) or a real HH:mm formatter to match. [isMine] defaults to [Message.isVendor] for
 * Customer's screens; Admin's screens pass [Message.isVendorMessage] instead - the two Compose
 * source files genuinely use different "is this my message" logic (see SalesAdminConversationView.kt/
 * SalesAdminNewMessageView.kt's `message.isVendorMessage()` vs Customer's `message.sender == "vendor"`).
 * [onImageClick]/[onPdfClick] default to no-ops - matching a genuine Compose inconsistency where
 * only the Conversation screens' attachments are tappable (full-screen preview / open externally);
 * the NewMessage screens render the same attachment bubbles with no tap handling at all. */
class CustomerMessageAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val counterpartName: () -> String,
    private val formatTimestamp: (String?) -> String,
    private val isMine: (Message) -> Boolean = { it.isVendor() },
    private val onImageClick: (String) -> Unit = {},
    private val onPdfClick: (String) -> Unit = {}
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
            val isMine = isMine(message)

            binding.textSenderLabel.text = if (isMine) context.getString(R.string.customer_message_me_label) else counterpartName()
            binding.textSenderLabel.setTextColor(
                context.getColor(if (isMine) R.color.karika_blue else R.color.karika_primary)
            )

            binding.bubbleContainer.setBackgroundResource(
                if (isMine) R.drawable.bg_bubble_mine else R.drawable.bg_bubble_other
            )

            renderAttachment(message)

            val text = message.message()
            binding.textMessageBubble.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textMessageBubble.text = text

            binding.textMessageTime.text = formatTimestamp(message.date())

            val gravity = if (isMine) Gravity.END else Gravity.START
            (binding.bubbleColumn.layoutParams as ConstraintLayout.LayoutParams).horizontalBias = if (isMine) 1f else 0f
            (binding.textSenderLabel.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.bubbleContainer.layoutParams as LinearLayout.LayoutParams).gravity = gravity
            (binding.textMessageTime.layoutParams as LinearLayout.LayoutParams).gravity = gravity
        }

        /** Mirrors composeApp's NewMessageAttachment/MessageAttachment: decode `images` as
         * [FileData], take the first filename, and branch on a ".pdf" suffix - everything else
         * is rendered as an image thumbnail loaded from HttpClientProvider.chatImage(filename). */
        private fun renderAttachment(message: Message) {
            val filename = message.images
                ?.takeIf { it.isNotEmpty() }
                ?.let { runCatching { Json.decodeFromString<FileData>(it) }.getOrNull() }
                ?.filename?.firstOrNull()?.takeIf { it.isNotEmpty() }

            if (filename == null) {
                binding.imageAttachment.visibility = View.GONE
                binding.rowAttachmentPdf.visibility = View.GONE
                return
            }

            if (filename.endsWith(".pdf", ignoreCase = true)) {
                // Mirrors composeApp's downloadReceipt(filename) -> openPdf(chatImage("/$it")) -
                // the leading slash before the filename produces a double slash in the final URL.
                val url = HttpClientProvider.chatImage("/$filename")
                binding.imageAttachment.visibility = View.GONE
                binding.rowAttachmentPdf.visibility = View.VISIBLE
                binding.textAttachmentPdfName.text = filename
                binding.rowAttachmentPdf.setOnClickListener { onPdfClick(url) }
            } else {
                val url = HttpClientProvider.chatImage(filename)
                binding.rowAttachmentPdf.visibility = View.GONE
                binding.imageAttachment.visibility = View.VISIBLE
                binding.imageAttachment.loadUrl(url, lifecycleOwner)
                binding.imageAttachment.setOnClickListener { onImageClick(url) }
            }
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
