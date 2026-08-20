package karika.distribucija.ba.salesrep.ui.orders

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderDetailBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderCommentBinding
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.model.VendorOrder
import karika.distribucija.ba.salesrep.model.VendorProduct
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import karika.distribucija.ba.salesrep.util.setHtmlText

/** Mirrors composeApp's ui/view/salesrep/orders/detail/SalesOrderDetailView.kt. */
class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderDetailViewModel by viewModels()

    private var deliveryExpanded = false
    private var selectedCarrierCode = ""
    private var shippingCostA2b: Double? = null
    private var shippingCostExpress: Double? = null
    private var shippingDefaultsApplied = false

    private val specColumnWidthsDp = intArrayOf(140, 80, 100, 90, 110, 130, 110, 100, 90)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scrollContent.applyImeBottomPadding()

        renderDeliveryExpanded()
        renderProviderSelection()
        renderProviderPrices()

        binding.buttonSendComment.setOnClickListener {
            val text = binding.editComment.text?.toString().orEmpty()
            if (text.isBlank()) return@setOnClickListener
            viewModel.sendComment(text)
            binding.editComment.setText("")
        }

        binding.buttonPrint.setOnClickListener { viewModel.printOrder() }

        binding.rowDeliveryHeader.setOnClickListener {
            deliveryExpanded = !deliveryExpanded
            renderDeliveryExpanded()
        }
        binding.rowProviderA2b.setOnClickListener {
            selectedCarrierCode = "A2B"
            renderProviderSelection()
        }
        binding.rowProviderExpress.setOnClickListener {
            selectedCarrierCode = "EURO_EXPRESS"
            renderProviderSelection()
        }
        binding.buttonCalculateShipping.setOnClickListener {
            val (a2b, express) = viewModel.calculateShipping(
                binding.editPackageWidth.text?.toString().orEmpty(),
                binding.editPackageHeight.text?.toString().orEmpty(),
                binding.editPackageDepth.text?.toString().orEmpty(),
                binding.editPackageWeight.text?.toString().orEmpty()
            )
            shippingCostA2b = a2b
            shippingCostExpress = express
            renderProviderPrices()
        }
        binding.buttonSaveShipping.setOnClickListener {
            viewModel.saveShippingDetails(
                VendorDeliveryServiceData(
                    name = binding.editContactName.text?.toString().orEmpty(),
                    email = binding.editContactEmail.text?.toString().orEmpty(),
                    telephone = binding.editContactPhone.text?.toString().orEmpty(),
                    city = binding.editCity.text?.toString().orEmpty(),
                    street = binding.editDeliveryAddress.text?.toString().orEmpty(),
                    postcode = binding.editPostalCode.text?.toString().orEmpty(),
                    weight = binding.editPackageWeight.text?.toString().orEmpty(),
                    width = binding.editPackageWidth.text?.toString().orEmpty(),
                    height = binding.editPackageHeight.text?.toString().orEmpty(),
                    depth = binding.editPackageDepth.text?.toString().orEmpty(),
                    note = binding.editDeliveryNote.text?.toString().orEmpty(),
                    companyCode = selectedCarrierCode
                )
            )
        }

        viewModel.vendorOrder.observe(viewLifecycleOwner) { order -> renderOrder(order) }
        viewModel.comments.observe(viewLifecycleOwner) { comments -> renderComments(comments) }

        viewModel.infoMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.layoutLoadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.isSendingComment.observe(viewLifecycleOwner) { sending ->
            binding.buttonSendComment.isEnabled = !sending
            binding.buttonSendComment.setBackgroundResource(
                if (sending) R.drawable.bg_send_button_disabled else R.drawable.bg_send_button
            )
            binding.iconSendComment.visibility = if (sending) View.GONE else View.VISIBLE
            binding.textSendComment.visibility = if (sending) View.GONE else View.VISIBLE
            binding.progressSendComment.visibility = if (sending) View.VISIBLE else View.GONE
        }

        viewModel.pdfUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.login_error_generic, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearPdfUrl()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderOrder(order: VendorOrder) {
        val dash = getString(R.string.order_detail_dash)

        binding.textOrderNumber.text = "#${order.orderId}"
        binding.textOrderDate.text = order.date()

        val vpcTotal = order.orderTotal?.toDoubleOrNull() ?: 0.0
        val pdvTotal = vpcTotal * 0.17
        val grandTotal = vpcTotal + pdvTotal
        val commission = order.shopCommissionFee?.toDoubleOrNull()
        val commissionText = if (commission != null) karikaPriceFormat(commission) + " KM" else dash

        binding.textVpcTotal.text = karikaPriceFormat(vpcTotal) + " KM"
        binding.textGrandTotal.text = karikaPriceFormat(grandTotal) + " KM"
        binding.textCommissionTotal.text = commissionText

        binding.textCustomerName.text = order.billingName ?: "Kupac #${order.customerId}"
        binding.textCustomerId.text = getString(R.string.order_detail_customer_id_format, order.customerId ?: "-")

        val address = order.address
        val addressText = listOfNotNull(address?.street, address?.city, address?.postcode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { dash }
        binding.textAddress.text = addressText
        binding.textPhone.text = address?.telephone?.takeIf { it.isNotBlank() } ?: dash

        val products = order.products
        val canEdit = order.isPending() && !order.locked()
        binding.textItemsCount.text = getString(R.string.order_detail_items_count_format, products.size)
        renderSpecification(products, canEdit)

        binding.textStatVpc.text = karikaPriceFormat(vpcTotal) + " KM"
        binding.textStatPdv.text = karikaPriceFormat(pdvTotal) + " KM"
        binding.textStatTotal.text = karikaPriceFormat(grandTotal) + " KM"
        binding.textStatCommission.text = commissionText

        binding.layoutDeliverySection.visibility = if (canEdit) View.VISIBLE else View.GONE
        if (canEdit && !shippingDefaultsApplied) {
            shippingDefaultsApplied = true
            val shipping = order.shippingDetails
            binding.editContactName.setText(shipping?.name.orEmpty())
            binding.editContactEmail.setText(shipping?.email.orEmpty())
            binding.editContactPhone.setText(shipping?.telephone.orEmpty())
            binding.editCity.setText(shipping?.city.orEmpty())
            binding.editDeliveryAddress.setText(shipping?.street.orEmpty())
            binding.editPostalCode.setText(shipping?.postcode.orEmpty())
            binding.editPackageWidth.setText(shipping?.width.orEmpty())
            binding.editPackageHeight.setText(shipping?.height.orEmpty())
            binding.editPackageDepth.setText(shipping?.depth.orEmpty())
            binding.editPackageWeight.setText(shipping?.weight.orEmpty())
            binding.editDeliveryNote.setText(shipping?.note.orEmpty())
            selectedCarrierCode = order.code.orEmpty()
            renderProviderSelection()
        }
    }

    private fun renderDeliveryExpanded() {
        binding.layoutDeliveryExpanded.visibility = if (deliveryExpanded) View.VISIBLE else View.GONE
        binding.iconDeliveryChevron.setImageResource(
            if (deliveryExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        )
    }

    private fun renderProviderSelection() {
        binding.rowProviderA2b.setBackgroundResource(
            if (selectedCarrierCode == "A2B") R.drawable.bg_provider_row_selected else android.R.color.transparent
        )
        binding.rowProviderExpress.setBackgroundResource(
            if (selectedCarrierCode == "EURO_EXPRESS") R.drawable.bg_provider_row_selected else android.R.color.transparent
        )
    }

    private fun renderProviderPrices() {
        val dash = getString(R.string.review_delivery_price_dash)
        binding.textPriceA2b.text = getString(
            R.string.review_delivery_price_format,
            shippingCostA2b?.let { karikaPriceFormat(it) + " KM" } ?: dash
        )
        binding.textPriceExpress.text = getString(
            R.string.review_delivery_price_format,
            shippingCostExpress?.let { karikaPriceFormat(it) + " KM" } ?: dash
        )
    }

    /** Mirrors composeApp's ProductSpecificationTable in SalesOrderDetailView.kt - a horizontally
     * scrollable table (not per-item cards), with an "AKCIJE"/Izmijeni column only while [canEdit]. */
    private fun renderSpecification(products: List<VendorProduct>, canEdit: Boolean) {
        binding.textNoItems.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        binding.scrollSpecTable.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        if (products.isEmpty()) return

        val widths = if (canEdit) specColumnWidthsDp else specColumnWidthsDp.dropLast(1).toIntArray()
        renderSpecificationHeader(canEdit, widths)
        renderSpecificationRows(products, canEdit, widths)
    }

    private fun renderSpecificationHeader(canEdit: Boolean, widths: IntArray) {
        binding.rowSpecHeader.removeAllViews()
        val headers = listOf(
            getString(R.string.review_spec_header_item),
            getString(R.string.review_spec_header_discount),
            getString(R.string.review_spec_header_price),
            getString(R.string.review_spec_header_qty),
            getString(R.string.review_spec_header_total_vpc),
            getString(R.string.review_spec_header_total_with_tax),
            getString(R.string.order_detail_spec_header_commission_percent),
            getString(R.string.review_spec_header_commission)
        ) + if (canEdit) listOf(getString(R.string.review_spec_header_actions)) else emptyList()

        headers.forEachIndexed { index, header ->
            binding.rowSpecHeader.addView(
                buildSpecCell(header, widths[index], color = R.color.karika_gray6, bold = true, sizeSp = 10f)
            )
            if (index != headers.lastIndex) binding.rowSpecHeader.addView(buildVerticalDivider())
        }
    }

    private fun renderSpecificationRows(products: List<VendorProduct>, canEdit: Boolean, widths: IntArray) {
        binding.tableRowsContainer.removeAllViews()
        products.forEachIndexed { rowIndex, product ->
            val price = product.price?.toDoubleOrNull() ?: 0.0
            val discountPercent = product.rabat().toIntOrNull() ?: 0
            val discountedPrice = price * (1.0 - discountPercent / 100.0)
            val qty = product.qtyOrdered?.toIntOrNull() ?: 0
            val rowTotalVpc = discountedPrice * qty
            val rowTotalWithPdv = rowTotalVpc * 1.17
            val commissionPercentText = karikaPriceFormat(product.commissionPercent?.toDoubleOrNull() ?: 0.0)
            val commissionText = karikaPriceFormat(product.commission?.toDoubleOrNull() ?: 0.0) + " KM"

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            row.addView(buildSpecCell(product.name ?: "—", widths[0], bold = true))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(product.rabat(), widths[1]))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(karikaPriceFormat(discountedPrice) + " KM", widths[2]))
            row.addView(buildVerticalDivider())
            row.addView(buildQuantityCell(product, widths[3]))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(karikaPriceFormat(rowTotalVpc) + " KM", widths[4]))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(karikaPriceFormat(rowTotalWithPdv) + " KM", widths[5]))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(commissionPercentText, widths[6]))
            row.addView(buildVerticalDivider())
            row.addView(buildSpecCell(commissionText, widths[7]))
            if (canEdit) {
                row.addView(buildVerticalDivider())
                row.addView(buildEditActionCell(widths[8]) { openEditItemSheet(product) })
            }

            binding.tableRowsContainer.addView(row)
            if (rowIndex != products.lastIndex) {
                binding.tableRowsContainer.addView(buildHorizontalDivider())
            }
        }
    }

    private fun buildSpecCell(
        text: String,
        widthDp: Int,
        bold: Boolean = false,
        color: Int = R.color.karika_gray2,
        sizeSp: Float = 13f
    ): TextView = TextView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(dp(widthDp), LinearLayout.LayoutParams.WRAP_CONTENT)
        this.text = text
        setTextColor(requireContext().getColor(color))
        textSize = sizeSp
        if (bold) setTypeface(typeface, Typeface.BOLD)
        maxLines = 2
        setPadding(dp(8), dp(10), dp(8), dp(10))
    }

    /** Stacks the struck-through original qty (if the item was edited) above the current qty,
     * matching composeApp's QuantityCell. */
    private fun buildQuantityCell(product: VendorProduct, widthDp: Int): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(dp(widthDp), LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }
        val original = product.originalQty()
        if (original.isNotEmpty()) {
            column.addView(TextView(requireContext()).apply {
                text = original
                setTextColor(requireContext().getColor(R.color.karika_gray7))
                textSize = 12f
                maxLines = 1
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            })
        }
        column.addView(TextView(requireContext()).apply {
            text = product.qty()
            setTextColor(requireContext().getColor(R.color.karika_gray2))
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
        })
        return column
    }

    private fun buildEditActionCell(widthDp: Int, onClick: () -> Unit): TextView = TextView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(dp(widthDp), LinearLayout.LayoutParams.WRAP_CONTENT)
        text = getString(R.string.review_spec_edit_action)
        setTextColor(requireContext().getColor(R.color.karika_blue))
        textSize = 13f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(8), dp(10), dp(8), dp(10))
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

    private fun buildVerticalDivider(): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(dp(1), LinearLayout.LayoutParams.MATCH_PARENT)
        setBackgroundColor(requireContext().getColor(R.color.karika_gray9))
    }

    private fun buildHorizontalDivider(): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
        setBackgroundColor(requireContext().getColor(R.color.karika_gray9))
    }

    private fun openEditItemSheet(item: VendorProduct) {
        EditOrderItemBottomSheet(
            item = item,
            canDiscount = viewModel.canCreateDiscountFor
        ) { newQty, newDiscount ->
            viewModel.editOrderProduct(item, newQty, newDiscount)
        }.show(parentFragmentManager, "EditOrderItemBottomSheet")
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics
    ).toInt()

    private fun renderComments(comments: List<Comment>) {
        binding.commentsContainer.removeAllViews()
        comments.forEach { comment -> binding.commentsContainer.addView(buildCommentView(comment)) }
        binding.commentsDivider.visibility = if (comments.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun buildCommentView(comment: Comment): View {
        val itemBinding = ItemOrderCommentBinding.inflate(layoutInflater, binding.commentsContainer, false)
        val isMine = comment.isMine()

        itemBinding.textCommentMessage.setHtmlText(comment.message())
        itemBinding.textCommentTime.text = comment.createdAt()
        itemBinding.textCommentMessage.setBackgroundResource(
            if (isMine) R.drawable.bg_comment_bubble_mine else R.drawable.bg_comment_bubble_other
        )
        itemBinding.textCommentMessage.setTextColor(
            requireContext().getColor(if (isMine) R.color.karika_white else R.color.karika_gray2)
        )

        val bias = if (isMine) 1f else 0f
        val gravity = if (isMine) Gravity.END else Gravity.START
        (itemBinding.bubbleColumn.layoutParams as ConstraintLayout.LayoutParams).horizontalBias = bias
        (itemBinding.textCommentMessage.layoutParams as LinearLayout.LayoutParams).gravity = gravity
        (itemBinding.textCommentTime.layoutParams as LinearLayout.LayoutParams).gravity = gravity

        return itemBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
