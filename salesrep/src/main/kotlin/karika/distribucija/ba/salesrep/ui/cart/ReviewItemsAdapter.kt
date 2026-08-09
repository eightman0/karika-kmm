package karika.distribucija.ba.salesrep.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemReviewBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem

/** Read-only line items for the review screen - qty/discount editing stays on the Cart screen. */
class ReviewItemsAdapter : ListAdapter<OnBehalfCartResponseItem, ReviewItemsAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isLast = position == itemCount - 1)
    }

    class ViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnBehalfCartResponseItem, isLast: Boolean) {
            binding.textName.text = item.name
            binding.textRowTotal.text = item.rowTotalString()
            val discount = item.discountPercent?.takeIf { it > 0 }
            binding.textDetail.text = if (discount != null) {
                "${item.qty} x ${item.priceString()} · Rabat $discount%"
            } else {
                "${item.qty} x ${item.priceString()}"
            }
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OnBehalfCartResponseItem>() {
            override fun areItemsTheSame(oldItem: OnBehalfCartResponseItem, newItem: OnBehalfCartResponseItem) =
                oldItem.itemId == newItem.itemId

            override fun areContentsTheSame(oldItem: OnBehalfCartResponseItem, newItem: OnBehalfCartResponseItem) =
                oldItem == newItem
        }
    }
}
