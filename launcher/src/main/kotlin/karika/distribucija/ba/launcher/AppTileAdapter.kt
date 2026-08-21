package karika.distribucija.ba.launcher

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppTileAdapter(
    private val apps: List<AppEntry>,
    private val packageManager: PackageManager,
    private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppTileAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.tile_icon)
        val label: TextView = view.findViewById(R.id.tile_label)
        val status: TextView = view.findViewById(R.id.tile_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_tile, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.label

        val installed = isInstalled(app.packageName)
        holder.icon.setImageDrawable(
            if (installed) {
                packageManager.getApplicationIcon(app.packageName)
            } else {
                holder.icon.context.getDrawable(android.R.drawable.sym_def_app_icon)
            }
        )
        holder.icon.alpha = if (installed) 1f else 0.4f
        holder.status.visibility = if (installed) View.GONE else View.VISIBLE

        holder.itemView.isEnabled = installed
        holder.itemView.setOnClickListener { onClick(app) }
    }

    private fun isInstalled(packageName: String): Boolean = try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
