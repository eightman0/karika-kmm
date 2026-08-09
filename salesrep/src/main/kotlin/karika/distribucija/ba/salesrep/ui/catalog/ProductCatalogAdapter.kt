package karika.distribucija.ba.salesrep.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemProductCatalogBinding
import karika.distribucija.ba.salesrep.model.OnBehalfProduct

class ProductCatalogAdapter(
    private val getQty: (OnBehalfProduct) -> Int,
    private val onQtyChanged: (OnBehalfProduct, Int) -> Unit
) : ListAdapter<OnBehalfProduct, ProductCatalogAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemProductCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Call after the shared cart changes so qty steppers reflect the latest server state. */
    fun refreshQuantities() {
        notifyItemRangeChanged(0, itemCount)
    }

    inner class ViewHolder(private val binding: ItemProductCatalogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: OnBehalfProduct) {
            binding.textName.text = product.name
            binding.textSku.text = product.sku
            binding.textPrice.text = product.priceString()

            val qty = getQty(product)
            binding.textQty.text = qty.toString()
            binding.textInCart.visibility = if (qty > 0) View.VISIBLE else View.GONE

            binding.buttonMinus.setOnClickListener {
                val newQty = (getQty(product) - 1).coerceAtLeast(0)
                binding.textQty.text = newQty.toString()
                binding.textInCart.visibility = if (newQty > 0) View.VISIBLE else View.GONE
                onQtyChanged(product, newQty)
            }
            binding.buttonPlus.setOnClickListener {
                val newQty = getQty(product) + 1
                binding.textQty.text = newQty.toString()
                binding.textInCart.visibility = View.VISIBLE
                onQtyChanged(product, newQty)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OnBehalfProduct>() {
            override fun areItemsTheSame(oldItem: OnBehalfProduct, newItem: OnBehalfProduct) =
                oldItem.entityId == newItem.entityId

            override fun areContentsTheSame(oldItem: OnBehalfProduct, newItem: OnBehalfProduct) =
                oldItem == newItem
        }
    }
}
