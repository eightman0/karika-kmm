package karika.distribucija.ba.salesrep.ui.customers

import android.content.res.ColorStateList
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

/** Mirrors SalesCustomersView.kt's CustomerCard: header (KLIJENT label + company/name + status
 * pill), then - for active and pending partnerships - a fading divider and a footer with the
 * assigned reps' avatars plus four actions (message/discount/history quiet buttons, "Naruči"
 * primary). Pending partnerships show the same footer dimmed with every action disabled. */
class CustomersAdapter(
    private val onClick: (OperationalCustomer) -> Unit,
    private val onOrderClick: (OperationalCustomer) -> Unit,
    private val onMessageClick: (OperationalCustomer) -> Unit,
    private val onDiscountClick: (OperationalCustomer) -> Unit,
    private val onHistoryClick: (OperationalCustomer) -> Unit,
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

            binding.textCustomerName.text = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
            val title = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
            binding.textKInitials.text = title.initials()
            val subtitle = listOfNotNull(
                customer.fullName.takeIf { it != "—" && !customer.company.isNullOrBlank() },
                customer.email?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            binding.textKSubtitle.text = subtitle
            binding.textKSubtitle.visibility = if (subtitle.isNotEmpty()) View.VISIBLE else View.GONE

            // Status pill (soft background + dot), sentence case like the Compose KPill
            binding.textBadge.text = customer.badgeLabel().lowercase().replaceFirstChar { it.uppercase() }
            val (badgeBg, badgeText, badgeDot) = when (customer.partnershipStatus) {
                "active" -> Triple(R.drawable.bg_k_pill_green_soft, R.color.k_green, R.color.k_green_dot)
                "pending" -> Triple(R.drawable.bg_k_pill_amber_soft, R.color.k_amber, R.color.k_amber)
                "rejected" -> Triple(R.drawable.bg_k_pill_red_soft, R.color.k_red, R.color.k_red)
                else -> Triple(R.drawable.bg_k_pill_field, R.color.k_muted, R.color.k_subtle)
            }
            binding.textBadge.setBackgroundResource(badgeBg)
            binding.textBadge.setTextColor(context.getColor(badgeText))
            binding.textBadge.compoundDrawableTintList = ColorStateList.valueOf(context.getColor(badgeDot))

            val showFooter = customer.isActive || customer.partnershipStatus == "pending"
            val actionsEnabled = customer.isActive

            binding.dividerFade.visibility = if (showFooter) View.VISIBLE else View.GONE
            binding.footerContainer.visibility = if (showFooter) View.VISIBLE else View.GONE
            binding.footerContainer.alpha = if (actionsEnabled) 1f else 0.5f
            if (showFooter) {
                bindReps(customer, actionsEnabled)
                binding.buttonOrder.isEnabled = actionsEnabled
                binding.buttonMessage.isEnabled = actionsEnabled
                binding.buttonDiscount.isEnabled = actionsEnabled
                binding.buttonHistory.isEnabled = actionsEnabled
                binding.buttonOrder.setOnClickListener { onOrderClick(customer) }
                binding.buttonMessage.setOnClickListener { onMessageClick(customer) }
                binding.buttonDiscount.setOnClickListener { onDiscountClick(customer) }
                binding.buttonHistory.setOnClickListener { onHistoryClick(customer) }
            }

            binding.root.setOnClickListener { onClick(customer) }
        }

        private fun bindReps(customer: OperationalCustomer, enabled: Boolean) {
            val context = binding.root.context
            val reps = customer.assignedEmployees.take(3)
            binding.repsAvatarContainer.removeAllViews()

            val hasReps = reps.isNotEmpty()
            binding.rowReps.visibility = if (hasReps) View.VISIBLE else View.GONE
            binding.spacerReps.visibility = if (hasReps) View.VISIBLE else View.GONE
            if (!hasReps) return

            val avatarSizePx = 26.dpToPx(context)
            val overlapPx = 16.dpToPx(context)
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
                        if (idx % 2 == 0) R.drawable.bg_k_cust_avatar_ring_accent else R.drawable.bg_k_cust_avatar_ring_ink
                    )
                    gravity = Gravity.CENTER
                    setTextColor(context.getColor(R.color.k_white))
                    setTypeface(typeface, Typeface.BOLD)
                    textSize = 9f
                    text = employee.displayName.initials()
                }
                binding.repsAvatarContainer.addView(avatar)
            }
            binding.repsAvatarContainer.isEnabled = enabled
            binding.repsAvatarContainer.setOnClickListener { onShowReps(customer.assignedEmployees) }
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
