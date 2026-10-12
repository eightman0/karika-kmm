package karika.distribucija.ba.salesrep.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.databinding.ItemNotificationsMarkAllBinding

/** "Označi sve kao pročitano" as the list's first row, shown only while something is unread -
 * same as the `if (hasUnread) item { ... }` in composeApp's SalesNotificationsView.kt. */
class MarkAllReadAdapter(
    private val onClick: () -> Unit
) : RecyclerView.Adapter<MarkAllReadAdapter.ViewHolder>() {

    var visible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) notifyItemInserted(0) else notifyItemRemoved(0)
        }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemNotificationsMarkAllBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.buttonMarkAll.setOnClickListener { onClick() }
    }

    class ViewHolder(val binding: ItemNotificationsMarkAllBinding) : RecyclerView.ViewHolder(binding.root)
}
