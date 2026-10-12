package karika.distribucija.ba.salesrep.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCartTitleBinding

/** "Artikli (n)" + "Isprazni korpu" row on top of the cart list - mirrors the title item in
 * composeApp's SalesOrderCartView.kt. Shown only while the cart has items, same as there. */
class CartTitleAdapter(
    private val onClear: () -> Unit
) : RecyclerView.Adapter<CartTitleAdapter.ViewHolder>() {

    private var count: Int = 0

    /** Updates the item count; the row disappears when the cart is empty. */
    fun setCount(value: Int) {
        val wasVisible = count > 0
        count = value
        when {
            wasVisible && value > 0 -> notifyItemChanged(0)
            wasVisible -> notifyItemRemoved(0)
            value > 0 -> notifyItemInserted(0)
        }
    }

    override fun getItemCount(): Int = if (count > 0) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCartTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.textTitle.text = binding.root.context.getString(R.string.k_cart2_items_title, count)
        binding.buttonClear.setOnClickListener { onClear() }
    }

    class ViewHolder(val binding: ItemCartTitleBinding) : RecyclerView.ViewHolder(binding.root)
}
