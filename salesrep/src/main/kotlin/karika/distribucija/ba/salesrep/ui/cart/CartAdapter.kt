package karika.distribucija.ba.salesrep.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemCartBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem

class CartAdapter(
    private val canDiscount: Boolean,
    private val onQtyChanged: (OnBehalfCartResponseItem, Int) -> Unit,
    private val onDiscountChanged: (OnBehalfCartResponseItem, Int) -> Unit,
    private val onRemove: (OnBehalfCartResponseItem) -> Unit
) : ListAdapter<OnBehalfCartResponseItem, CartAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var discountWatcher: android.text.TextWatcher? = null

        fun bind(item: OnBehalfCartResponseItem) {
            binding.textName.text = item.name
            binding.textSku.text = "${item.sku} · ${item.priceString()}"
            binding.textQty.text = item.qty.toString()
            binding.textRowTotal.text = item.rowTotalString()

            binding.layoutDiscount.visibility = if (canDiscount) View.VISIBLE else View.GONE
            discountWatcher?.let { binding.editDiscount.removeTextChangedListener(it) }
            binding.editDiscount.setText(item.discountPercent?.takeIf { it > 0 }?.toString() ?: "")
            discountWatcher = binding.editDiscount.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                onDiscountChanged(item, text?.toString()?.toIntOrNull() ?: 0)
            })

            binding.buttonMinus.setOnClickListener {
                val newQty = item.qty - 1
                onQtyChanged(item, newQty)
            }
            binding.buttonPlus.setOnClickListener {
                onQtyChanged(item, item.qty + 1)
            }
            binding.buttonRemove.setOnClickListener { onRemove(item) }
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
