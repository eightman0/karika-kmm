package karika.distribucija.ba.salesrep.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCartBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import karika.distribucija.ba.salesrep.util.loadUrl

/** Mirrors composeApp's CartItemRow in SalesOrderCartView.kt. */
class CartAdapter(
    private val lifecycleOwner: LifecycleOwner,
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

        private var qtyWatcher: android.text.TextWatcher? = null
        private var discountWatcher: android.text.TextWatcher? = null

        fun bind(item: OnBehalfCartResponseItem) {
            val context = binding.root.context

            binding.imageProduct.loadUrl(item.imageUrl, lifecycleOwner)
            binding.textName.text = item.name
            val hasSku = item.sku.isNotBlank()
            binding.textSku.visibility = if (hasSku) View.VISIBLE else View.GONE
            if (hasSku) binding.textSku.text = context.getString(R.string.catalog_sku_format, item.sku)

            val discountPercent = item.discountPercent ?: 0
            val vpc = item.price * item.qty
            binding.textVpcStrike.visibility = if (discountPercent > 0) View.VISIBLE else View.GONE
            binding.textVpcStrike.text = karikaPriceFormat(vpc) + " KM"
            binding.textVpcStrike.paintFlags =
                binding.textVpcStrike.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            binding.textPrice.text = item.rowTotalString()

            qtyWatcher?.let { binding.editQty.removeTextChangedListener(it) }
            binding.editQty.setText(item.qty.toString())
            qtyWatcher = binding.editQty.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                val n = text?.toString()?.toIntOrNull()
                if (n != null && n > 0) onQtyChanged(item, n)
            })
            binding.buttonMinus.setOnClickListener {
                if (item.qty > 1) onQtyChanged(item, item.qty - 1)
            }
            binding.buttonPlus.setOnClickListener {
                onQtyChanged(item, item.qty + 1)
            }

            binding.layoutDiscount.visibility = if (canDiscount) View.VISIBLE else View.GONE
            binding.textDiscountReadonly.visibility =
                if (!canDiscount && discountPercent > 0) View.VISIBLE else View.GONE
            if (!canDiscount && discountPercent > 0) {
                binding.textDiscountReadonly.text =
                    context.getString(R.string.cart_discount_readonly_format, discountPercent)
            }

            discountWatcher?.let { binding.editDiscount.removeTextChangedListener(it) }
            binding.editDiscount.setText(discountPercent.takeIf { it > 0 }?.toString() ?: "")
            discountWatcher = binding.editDiscount.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                onDiscountChanged(item, text?.toString()?.toIntOrNull()?.coerceAtMost(100) ?: 0)
            })

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
