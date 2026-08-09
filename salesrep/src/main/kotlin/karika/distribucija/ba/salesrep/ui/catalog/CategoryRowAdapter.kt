package karika.distribucija.ba.salesrep.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCategoryRowBinding
import karika.distribucija.ba.salesrep.model.Category

/** Pre-computed row state for one CategorySheet list item - covers both hierarchical navigation
 * rows (arrow shown when the category has children) and flat deep-search result rows
 * (breadcrumb shown instead), mirroring SalesOrderCatalogView.kt's CategorySheet. */
data class CategoryRow(
    val category: Category,
    val breadcrumb: String?,
    val hasChildren: Boolean,
    val isSelected: Boolean
)

class CategoryRowAdapter(
    private val onClick: (CategoryRow) -> Unit
) : RecyclerView.Adapter<CategoryRowAdapter.ViewHolder>() {

    private var items: List<CategoryRow> = emptyList()

    fun submitList(newItems: List<CategoryRow>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: CategoryRow) {
            binding.textName.text = row.category.name
            binding.textBreadcrumb.visibility = if (row.breadcrumb != null) View.VISIBLE else View.GONE
            binding.textBreadcrumb.text = row.breadcrumb
            binding.iconArrow.visibility = if (row.hasChildren && row.breadcrumb == null) View.VISIBLE else View.GONE

            val context = binding.root.context
            binding.root.setBackgroundResource(
                if (row.isSelected) R.drawable.bg_category_row_selected else R.drawable.bg_category_row_unselected
            )
            binding.textName.setTextColor(
                context.getColor(if (row.isSelected) R.color.karika_blue else R.color.karika_gray2)
            )
            binding.root.setOnClickListener { onClick(row) }
        }
    }
}
