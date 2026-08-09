package karika.distribucija.ba.salesrep.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemProductCatalogBinding
import karika.distribucija.ba.salesrep.model.OnBehalfProduct
import karika.distribucija.ba.salesrep.util.loadUrl

/** Mirrors composeApp's ProductCard in SalesOrderCatalogView.kt: the stepper only adjusts a
 * local quantity, the Dodaj/Ažuriraj button is what actually commits it via [onAdd]. */
class ProductCatalogAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val getQty: (OnBehalfProduct) -> Int,
    private val onAdd: (OnBehalfProduct, Int) -> Unit
) : ListAdapter<OnBehalfProduct, ProductCatalogAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemProductCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Call after the shared cart changes so quantity badges/labels reflect the latest server state. */
    fun refreshQuantities() {
        notifyItemRangeChanged(0, itemCount)
    }

    inner class ViewHolder(private val binding: ItemProductCatalogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: OnBehalfProduct) {
            val context = binding.root.context
            val cartQty = getQty(product)
            val initialQty = if (cartQty > 0) cartQty else product.minQty().coerceAtLeast(1)
            binding.editQty.setText(initialQty.toString())

            binding.imageProduct.loadUrl(product.imageUrl, lifecycleOwner)
            binding.badgeInCart.visibility = if (cartQty > 0) View.VISIBLE else View.GONE

            binding.textName.text = product.name
            val hasSku = product.sku.isNotBlank()
            binding.textSku.visibility = if (hasSku) View.VISIBLE else View.GONE
            if (hasSku) binding.textSku.text = context.getString(R.string.catalog_sku_format, product.sku)

            val categoryLabel = product.categoryLabel
            binding.textCategory.visibility = if (!categoryLabel.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.textCategory.text = categoryLabel

            binding.textPrice.text = product.priceString()
            binding.textAdd.text = context.getString(
                if (cartQty > 0) R.string.catalog_update else R.string.catalog_add
            )

            binding.buttonMinus.setOnClickListener {
                val current = binding.editQty.text?.toString()?.toIntOrNull() ?: initialQty
                if (current > 1) binding.editQty.setText((current - 1).toString())
            }
            binding.buttonPlus.setOnClickListener {
                val current = binding.editQty.text?.toString()?.toIntOrNull() ?: initialQty
                binding.editQty.setText((current + 1).toString())
            }
            binding.buttonAdd.setOnClickListener {
                val current = binding.editQty.text?.toString()?.toIntOrNull()?.takeIf { it > 0 } ?: initialQty
                onAdd(product, current)
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
