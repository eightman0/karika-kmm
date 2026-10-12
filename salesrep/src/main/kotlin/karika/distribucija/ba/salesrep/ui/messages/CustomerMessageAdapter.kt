package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCustomerMessageBinding
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import karika.distribucija.ba.salesrep.util.loadUrl

/** Mirrors composeApp's SalesChatBubble - shared by customer and admin messaging (both bind
 * against Chat V2's ChatMessage). The sender name (other side only), attachments, body and the
 * time all sit inside the bubble. [senderName] gives each screen its own label (the message's
 * sender_display_name for customers, the literal "Administrator" for admin), [formatTimestamp]
 * its own time format (full Sarajevo date-time on the conversations, HH:mm on the new-message
 * screens). "Mine" is [ChatMessage.isFromVendor], as on both Compose views. */
class CustomerMessageAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val senderName: (ChatMessage) -> String?,
    private val formatTimestamp: (String?) -> String,
    private val onImageClick: (String) -> Unit = {},
    private val onPdfClick: (String) -> Unit = {}
) : ListAdapter<ChatMessage, CustomerMessageAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCustomerMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val context = binding.root.context
            val isMine = message.isFromVendor()

            // Sender name only on the other side's bubble, inside it (SalesChatBubble).
            val sender = if (isMine) "" else senderName(message).orEmpty()
            binding.textSenderLabel.visibility = if (sender.isNotBlank()) View.VISIBLE else View.GONE
            binding.textSenderLabel.text = sender

            binding.bubbleContainer.setBackgroundResource(
                if (isMine) R.drawable.bg_k_msg_bubble_mine else R.drawable.bg_k_msg_bubble_other
            )
            binding.textMessageBubble.setTextColor(context.getColor(if (isMine) R.color.k_white else R.color.k_ink))
            // PDF chip: translucent white on the blue bubble, soft blue on the white one.
            binding.rowAttachmentPdf.setBackgroundResource(
                if (isMine) R.drawable.bg_k_msg_pdf_mine else R.drawable.bg_k_msg_soft_10
            )
            val pdfColor = context.getColor(if (isMine) R.color.k_white else R.color.k_accent)
            binding.textAttachmentPdfName.setTextColor(pdfColor)
            binding.iconAttachmentPdf.setColorFilter(pdfColor)

            renderAttachment(message)

            val text = message.message()
            binding.textMessageBubble.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textMessageBubble.text = text.asMessageBody()

            // Time inside the bubble: white 80% on the blue bubble, k_subtle on the white one.
            val time = formatTimestamp(message.createdAt)
            binding.textMessageTime.visibility = if (time.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textMessageTime.text = time
            binding.textMessageTime.setTextColor(
                context.getColor(if (isMine) R.color.k_msg_time_mine else R.color.k_subtle)
            )

            binding.bubbleContainer.alignBubble(isMine)
        }

        /** Mirrors SalesChatAttachments: a PDF as a chip opening the in-app viewer, anything
         * else as an image thumbnail, both from the attachment's relpath. The bubble has one slot
         * of each, so the first image and the first PDF are shown. */
        private fun renderAttachment(message: ChatMessage) {
            val withPath = message.attachments.filter { !it.relpath.isNullOrEmpty() }
            val pdf = withPath.firstOrNull { it.isPdf() }
            val image = withPath.firstOrNull { !it.isPdf() }

            binding.rowAttachmentPdf.visibility = if (pdf != null) View.VISIBLE else View.GONE
            if (pdf != null) {
                val url = HttpClientProvider.chatAttachment(pdf.relpath.orEmpty())
                binding.textAttachmentPdfName.text = pdf.filename.orEmpty()
                binding.rowAttachmentPdf.setOnClickListener { onPdfClick(url) }
            }

            binding.imageAttachment.visibility = if (image != null) View.VISIBLE else View.GONE
            if (image != null) {
                val url = HttpClientProvider.chatAttachment(image.relpath.orEmpty())
                binding.imageAttachment.clipToOutline = true
                binding.imageAttachment.loadUrl(url, lifecycleOwner)
                binding.imageAttachment.setOnClickListener { onImageClick(url) }
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem.messageId == newItem.messageId

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem == newItem
        }
    }
}
