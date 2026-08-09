package karika.distribucija.ba.salesrep.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemProductSearchBinding
import karika.distribucija.ba.salesrep.model.OnBehalfProduct

class ProductSearchAdapter(
    private val onClick: (OnBehalfProduct) -> Unit
) : RecyclerView.Adapter<ProductSearchAdapter.ViewHolder>() {

    private var items: List<OnBehalfProduct> = emptyList()

    fun submitList(newItems: List<OnBehalfProduct>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemProductSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemProductSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: OnBehalfProduct) {
            binding.textName.text = product.name
            binding.textSku.text = product.sku
            binding.root.setOnClickListener { onClick(product) }
        }
    }
}
