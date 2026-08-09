package karika.distribucija.ba.salesrep.ui.customers

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCustomerBinding
import karika.distribucija.ba.salesrep.model.AssignedEmployeeSummary
import karika.distribucija.ba.salesrep.model.OperationalCustomer

/** "Ada Lovelace" -> "AL", "Ada" -> "A". Mirrors SalesCustomersView.kt's private initials(). */
private fun String?.initials(): String =
    this?.trim()?.split(" ")?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("") ?: "?"

class CustomersAdapter(
    private val onClick: (OperationalCustomer) -> Unit,
    private val onOrderClick: (OperationalCustomer) -> Unit,
    private val onShowReps: (List<AssignedEmployeeSummary>) -> Unit
) : ListAdapter<OperationalCustomer, CustomersAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: OperationalCustomer) {
            val context = binding.root.context

            binding.textCustomerName.text = customer.fullName
            binding.textBadge.text = customer.badgeLabel()
            if (customer.isActive) {
                binding.textBadge.setBackgroundResource(R.drawable.bg_customer_badge_active)
                binding.textBadge.setTextColor(context.getColor(R.color.karika_green3))
            } else {
                binding.textBadge.setBackgroundResource(R.drawable.bg_customer_badge_other)
                binding.textBadge.setTextColor(context.getColor(R.color.karika_blue))
            }

            val hasCompany = !customer.company.isNullOrBlank()
            binding.rowCompany.visibility = if (hasCompany) View.VISIBLE else View.GONE
            binding.spacerCompany.visibility = if (hasCompany) View.VISIBLE else View.GONE
            binding.textCompany.text = customer.company.orEmpty()

            val hasEmail = !customer.email.isNullOrBlank()
            binding.rowEmail.visibility = if (hasEmail) View.VISIBLE else View.GONE
            binding.textEmail.text = customer.email.orEmpty()

            binding.footerContainer.visibility = if (customer.isActive) View.VISIBLE else View.GONE
            if (customer.isActive) {
                bindReps(customer)
                binding.buttonOrder.setOnClickListener { onOrderClick(customer) }
            }

            binding.root.setOnClickListener { onClick(customer) }
        }

        private fun bindReps(customer: OperationalCustomer) {
            val context = binding.root.context
            val reps = customer.assignedEmployees.take(3)
            binding.repsAvatarContainer.removeAllViews()

            if (reps.isNotEmpty()) {
                binding.repsAvatarContainer.visibility = View.VISIBLE
                binding.textRepsLabel.visibility = View.VISIBLE
                binding.textUnassigned.visibility = View.GONE

                val avatarSizePx = 26.dpToPx(context)
                val overlapPx = 10.dpToPx(context)
                binding.repsAvatarContainer.layoutParams =
                    binding.repsAvatarContainer.layoutParams.apply {
                        width = avatarSizePx + overlapPx * (reps.size - 1)
                    }

                reps.forEachIndexed { idx, employee ->
                    val avatar = TextView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(avatarSizePx, avatarSizePx).apply {
                            leftMargin = idx * overlapPx
                        }
                        setBackgroundResource(
                            if (idx % 2 == 0) R.drawable.bg_avatar_blue else R.drawable.bg_avatar_secondary
                        )
                        gravity = Gravity.CENTER
                        setTextColor(context.getColor(R.color.karika_white))
                        setTypeface(typeface, Typeface.BOLD)
                        textSize = 8f
                        text = employee.displayName.initials()
                    }
                    binding.repsAvatarContainer.addView(avatar)
                }
                binding.repsAvatarContainer.setOnClickListener { onShowReps(customer.assignedEmployees) }
            } else if (customer.company.isNullOrBlank()) {
                binding.repsAvatarContainer.visibility = View.GONE
                binding.textRepsLabel.visibility = View.GONE
                binding.textUnassigned.visibility = View.VISIBLE
            } else {
                binding.repsAvatarContainer.visibility = View.GONE
                binding.textRepsLabel.visibility = View.GONE
                binding.textUnassigned.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OperationalCustomer>() {
            override fun areItemsTheSame(oldItem: OperationalCustomer, newItem: OperationalCustomer) =
                oldItem.customerId == newItem.customerId

            override fun areContentsTheSame(oldItem: OperationalCustomer, newItem: OperationalCustomer) =
                oldItem == newItem
        }

        private fun Int.dpToPx(context: android.content.Context): Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
