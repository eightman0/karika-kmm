package karika.distribucija.ba.salesrep.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemDiscountBinding
import karika.distribucija.ba.salesrep.model.DiscountRule

class DiscountsAdapter(
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
            binding.textTarget.text = rule.targetLabel()
            binding.textApproval.text = rule.approvalLabel()
            binding.textApproval.background.setTint(approvalColor(rule.approvalStatus))
            val minQty = rule.minQty?.toInt()
            val percent = rule.discountPercent.toInt()
            binding.textRule.text = if (minQty != null && minQty > 0) {
                "Min. kol. $minQty · Rabat $percent%"
            } else {
                "Rabat $percent%"
            }
            binding.buttonEdit.setOnClickListener { onEdit(rule) }
            binding.buttonDelete.setOnClickListener { onDelete(rule) }
        }

        private fun approvalColor(status: String?): Int {
            val colorRes = when (status) {
                "approved" -> R.color.status_approved
                "pending" -> R.color.status_pending
                "rejected" -> R.color.status_rejected
                else -> R.color.status_default
            }
            return binding.root.context.getColor(colorRes)
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
