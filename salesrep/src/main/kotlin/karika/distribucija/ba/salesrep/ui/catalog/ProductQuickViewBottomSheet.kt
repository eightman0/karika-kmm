package karika.distribucija.ba.salesrep.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.OnBehalfProduct
import karika.distribucija.ba.salesrep.util.applyWhiteSheetBackground
import karika.distribucija.ba.salesrep.util.loadUrl

/** Product "brzi pregled" sheet, shown when tapping a catalog item's image/info area. */
class ProductQuickViewBottomSheet(
    private val product: OnBehalfProduct
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_product_quick_view, container, false)

    override fun onStart() {
        super.onStart()
        applyWhiteSheetBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        view.findViewById<View>(R.id.button_close).setOnClickListener { dismiss() }

        view.findViewById<ImageView>(R.id.image_product).loadUrl(product.imageUrl, viewLifecycleOwner)

        view.findViewById<TextView>(R.id.text_stock_badge).apply {
            if (product.isInStock) {
                text = getString(R.string.catalog_stock_in)
                setBackgroundResource(R.drawable.bg_customer_badge_active)
                setTextColor(context.getColor(R.color.karika_green3))
            } else {
                text = getString(R.string.catalog_stock_out)
                setBackgroundResource(R.drawable.bg_detail_badge_rejected)
                setTextColor(context.getColor(R.color.karika_error))
            }
        }
        view.findViewById<TextView>(R.id.text_available_qty).text = getString(
            R.string.catalog_quick_view_available_format,
            product.salableQty?.toInt() ?: 0
        )

        view.findViewById<TextView>(R.id.text_name).text = product.name

        val categoryLabel = product.categoryLabel
        view.findViewById<TextView>(R.id.text_category).apply {
            visibility = if (!categoryLabel.isNullOrBlank()) View.VISIBLE else View.GONE
            if (!categoryLabel.isNullOrBlank()) {
                text = getString(R.string.catalog_quick_view_category_format, categoryLabel)
            }
        }

        view.findViewById<TextView>(R.id.text_sku).text = product.sku
        view.findViewById<TextView>(R.id.text_price).text = product.priceString()

        view.findViewById<TextView>(R.id.text_description).text =
            product.description?.takeIf { it.isNotBlank() }
                ?: getString(R.string.catalog_quick_view_description_empty)
    }
}
