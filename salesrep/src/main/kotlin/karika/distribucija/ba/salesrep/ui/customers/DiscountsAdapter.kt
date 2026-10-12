package karika.distribucija.ba.salesrep.ui.customers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemDiscountBinding
import karika.distribucija.ba.salesrep.model.DiscountRule

class DiscountsAdapter(
    private val canEdit: Boolean,
    private val onEdit: (DiscountRule) -> Unit,
    private val onDelete: (DiscountRule) -> Unit
) : ListAdapter<DiscountRule, DiscountsAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemDiscountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDiscountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: DiscountRule) {
            val context = binding.root.context

            binding.textTarget.text = rule.targetLabel()
            binding.textApproval.text = rule.approvalLabel().lowercase().replaceFirstChar { it.uppercase() }
            binding.textApproval.setBackgroundResource(approvalBgRes(rule.approvalStatus))
            binding.textApproval.setTextColor(context.getColor(approvalColorRes(rule.approvalStatus)))

            // Detail line like the Compose DiscountCard: "Kategorija · min. 5 kom"
            binding.textMinQty.text = buildList {
                add(
                    when {
                        rule.productId != null -> "Artikal"
                        rule.categoryId != null -> "Kategorija"
                        else -> "Svi artikli"
                    }
                )
                rule.minQty?.toInt()?.let { add("min. $it kom") }
            }.joinToString(" · ")
            binding.textPercent.text = "${rule.discountPercent.toInt()}%"

            binding.buttonEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.buttonDelete.visibility = if (canEdit) View.VISIBLE else View.GONE
            binding.buttonEdit.setOnClickListener { onEdit(rule) }
            binding.buttonDelete.setOnClickListener { onDelete(rule) }
        }

        // Mirrors SalesCustomerDetailView.kt's DiscountCard approval Triple.
        private fun approvalBgRes(status: String?): Int = when (status) {
            "approved" -> R.drawable.bg_k_pill_green_soft
            "pending" -> R.drawable.bg_k_pill_amber_soft
            "rejected" -> R.drawable.bg_k_pill_red_soft
            else -> R.drawable.bg_k_pill_field
        }

        private fun approvalColorRes(status: String?): Int = when (status) {
            "approved" -> R.color.k_green
            "pending" -> R.color.k_amber
            "rejected" -> R.color.k_red
            else -> R.color.k_muted
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DiscountRule>() {
            override fun areItemsTheSame(oldItem: DiscountRule, newItem: DiscountRule) =
                oldItem.ruleId == newItem.ruleId

            override fun areContentsTheSame(oldItem: DiscountRule, newItem: DiscountRule) =
                oldItem == newItem
        }
    }
}
