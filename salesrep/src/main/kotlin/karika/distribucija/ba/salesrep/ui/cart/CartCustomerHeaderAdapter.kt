package karika.distribucija.ba.salesrep.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemCartCustomerHeaderBinding
import karika.distribucija.ba.salesrep.ui.messages.messageInitials

/** The single customer card above the cart rows - mirrors CustomerHeader in composeApp's
 * SalesOrderCartView.kt. Shown only while the cart has items, same as there. */
class CartCustomerHeaderAdapter(
    private val customerLabel: String,
    private val email: String?
) : RecyclerView.Adapter<CartCustomerHeaderAdapter.ViewHolder>() {

    var visible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) notifyItemInserted(0) else notifyItemRemoved(0)
        }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCartCustomerHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.textInitials.text = customerLabel.messageInitials()
        binding.textCustomer.text = customerLabel
        binding.textEmail.visibility = if (email.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.textEmail.text = email
    }

    class ViewHolder(val binding: ItemCartCustomerHeaderBinding) : RecyclerView.ViewHolder(binding.root)
}
