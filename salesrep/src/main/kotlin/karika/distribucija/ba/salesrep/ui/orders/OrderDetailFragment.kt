package karika.distribucija.ba.salesrep.ui.orders

import karika.distribucija.ba.salesrep.util.KSnackbar
import karika.distribucija.ba.salesrep.util.showSnack
import android.content.res.ColorStateList
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderDetailBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderCommentBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderSpecItemBinding
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.CommentFile
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.model.VendorOrder
import karika.distribucija.ba.salesrep.model.VendorProduct
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import karika.distribucija.ba.salesrep.ui.messages.ImagePreviewFragment
import karika.distribucija.ba.salesrep.ui.messages.messageInitials
import karika.distribucija.ba.salesrep.ui.pdf.PdfViewerActivity
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import karika.distribucija.ba.salesrep.util.loadUrl
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
            AnalyticsTracker.trackClick("order_detail", "send_comment")
            viewModel.sendComment(text)
            binding.editComment.setText("")
        }

        binding.buttonPrint.setOnClickListener { anchor ->
            AnalyticsTracker.trackClick("order_detail", "print_order")
            if (viewModel.vendorOrder.value?.isPending() == true) {
                showPrintMenu(anchor)
            } else {
                viewModel.printOrder()
            }
        }

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
            if (message != null) showSnack(message, KSnackbar.Type.SUCCESS)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.layoutLoadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.isSendingComment.observe(viewLifecycleOwner) { sending ->
            binding.buttonSendComment.isEnabled = !sending
            // composeApp swaps the button for a field-colored box with an accent spinner.
            binding.buttonSendComment.setBackgroundResource(
                if (sending) R.drawable.bg_k_field else R.drawable.bg_k_button_primary
            )
            binding.layoutSendCommentLabel.visibility = if (sending) View.GONE else View.VISIBLE
            binding.progressSendComment.visibility = if (sending) View.VISIBLE else View.GONE
        }

        viewModel.pdfUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                PdfViewerActivity.open(requireContext(), url, getString(R.string.order_detail_title_format, viewModel.vendorOrder.value?.orderId.orEmpty()))
                viewModel.clearPdfUrl()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showSnack(message)
            }
        }
    }

    private fun renderOrder(order: VendorOrder) {
        val dash = getString(R.string.order_detail_dash)
        val locked = order.locked()
        val canEdit = order.isPending() && !locked

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.order_detail_title_format, order.orderId.orEmpty())

        binding.buttonPrint.visibility = if (order.isRejected()) View.GONE else View.VISIBLE
        binding.buttonPrint.setImageResource(if (order.isPending()) R.drawable.ic_k_ord_fab_menu else R.drawable.ic_print)

        binding.layoutLockedNotice.visibility = if (locked) View.VISIBLE else View.GONE

        // Same totals as composeApp's SalesOrderDetailView: the order's own VPC total + 17% PDV.
        val vpcTotal = order.orderTotal?.toDoubleOrNull() ?: 0.0
        val pdvTotal = vpcTotal * 0.17
        val grandTotal = vpcTotal + pdvTotal
        val commission = order.shopCommissionFee?.toDoubleOrNull()
        val commissionText = if (commission != null) karikaPriceFormat(commission) + " KM" else dash

        // ── OrderSummary ──
        binding.textOrderNumber.text = "#${order.orderId.orEmpty()}"
        binding.textOrderDate.text = order.date()
        binding.textGrandTotal.text = karikaPriceFormat(grandTotal) + " KM"
        binding.textVpcTotal.text = karikaPriceFormat(vpcTotal) + " KM"
        binding.textCommissionTotal.text = commissionText
        renderStatusPill(order)

        // ── CustomerSection ──
        val name = order.b2bPravnoLice ?: getString(R.string.order_detail_customer_fallback, order.customerId.orEmpty())
        binding.textCustomerInitials.text = name.messageInitials()
        binding.textCustomerName.text = name
        binding.textCustomerContact.visibility = if (order.billingName.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.textCustomerContact.text = getString(R.string.order_detail_contact_person_suffix, order.billingName.orEmpty())

        val address = order.address?.let { a ->
            listOfNotNull(a.street, a.city, a.postcode).filter { it.isNotBlank() }.joinToString(", ")
        }.takeIf { !it.isNullOrBlank() } ?: dash
        val phone = order.address?.telephone.takeIf { !it.isNullOrBlank() } ?: dash
        // No Pozovi / Email buttons (composeApp has them): the app runs kiosk-locked and must
        // not hand off to a dialer or mail app.
        renderKeyValueRows(
            binding.layoutCustomerInfo,
            listOf(
                getString(R.string.order_detail_kv_pdv) to (order.pdvNumber ?: "-"),
                getString(R.string.order_detail_kv_id) to (order.idNumber ?: "-"),
                getString(R.string.order_detail_kv_contact) to (order.billingName ?: "-"),
                getString(R.string.order_detail_kv_email) to order.email(),
                getString(R.string.order_detail_kv_phone) to phone,
                getString(R.string.order_detail_kv_address) to address
            )
        )

        // ── Specifikacija ──
        val products = order.products
        binding.textItemsCount.text = itemsLabel(products.size)
        renderSpecification(products, canEdit)
        renderSpecTotals(vpcTotal, pdvTotal, grandTotal, commissionText)

        // Locked: the order's data stays blurred, as composeApp's lockedBlur().
        listOf(
            binding.textGrandTotal, binding.layoutSummaryCommission, binding.layoutCustomerHeader,
            binding.layoutCustomerInfo, binding.layoutSpecCard
        ).forEach { it.setLockedBlur(locked) }

        binding.textCommentsArchived.visibility = if (order.commentsArchived()) View.VISIBLE else View.GONE
        binding.layoutCommentForm.visibility = if (order.commentsArchived()) View.GONE else View.VISIBLE

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

        // The customer's bubbles carry the company name from the order, which can arrive after
        // the comments did.
        viewModel.comments.value?.let { renderComments(it) }
    }

    /** KPill on the navy card - light status color at 15% behind a dot and the label. */
    private fun renderStatusPill(order: VendorOrder) {
        val color = when (order.realOrderStatus) {
            "approved", "bill-sent" -> 0xFF86EFAC.toInt()
            "pending", "estimate-sent" -> 0xFFFCD34D.toInt()
            "rejected" -> 0xFFFCA5A5.toInt()
            else -> 0xFFD1D5DB.toInt()
        }
        val translucent = (color and 0x00FFFFFF) or (0x26 shl 24)
        binding.layoutStatusPill.background = GradientDrawable().apply {
            cornerRadius = dp(50).toFloat()
            setColor(translucent)
        }
        binding.viewStatusDot.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        binding.textStatus.setTextColor(color)
        binding.textStatus.text = order.status()
    }

    /** KKeyValueCard / KKeyValueRow: muted label left, bold value right, dividers between. */
    private fun renderKeyValueRows(container: LinearLayout, rows: List<Pair<String, String?>>) {
        container.removeAllViews()
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) container.addView(buildHorizontalDivider())
            container.addView(buildKeyValueRow(label, value))
        }
    }

    private fun buildKeyValueRow(
        label: String,
        value: String?,
        labelColor: Int = R.color.k_muted,
        bold: Boolean = false,
        valueSizeSp: Float = 14f
    ): View {
        val context = requireContext()
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
        }
        row.addView(TextView(context).apply {
            text = label
            setTextColor(context.getColor(labelColor))
            textSize = 14f
            maxLines = 1
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        row.addView(TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12)
            }
            text = value?.takeIf { it.isNotBlank() } ?: getString(R.string.order_detail_dash)
            setTextColor(context.getColor(R.color.k_ink))
            textSize = valueSizeSp
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            maxLines = 2
        })
        return row
    }

    private fun renderSpecTotals(vpcTotal: Double, pdvTotal: Double, grandTotal: Double, commissionText: String) {
        val container = binding.layoutSpecTotals
        container.removeAllViews()
        container.addView(buildHorizontalDivider())
        container.addView(buildKeyValueRow(getString(R.string.order_detail_total_vpc), karikaPriceFormat(vpcTotal) + " KM"))
        container.addView(buildHorizontalDivider())
        container.addView(buildKeyValueRow(getString(R.string.order_detail_total_pdv17), karikaPriceFormat(pdvTotal) + " KM"))
        container.addView(buildHorizontalDivider())
        container.addView(buildKeyValueRow(getString(R.string.order_detail_karika_commission), commissionText))
        container.addView(buildHorizontalDivider())
        container.addView(
            buildKeyValueRow(
                getString(R.string.order_detail_total_with_pdv),
                karikaPriceFormat(grandTotal) + " KM",
                labelColor = R.color.k_ink,
                bold = true,
                valueSizeSp = 15f
            )
        )
    }

    private fun View.setLockedBlur(locked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setRenderEffect(
                if (locked) RenderEffect.createBlurEffect(dp(5).toFloat(), dp(5).toFloat(), Shader.TileMode.CLAMP) else null
            )
        } else {
            alpha = if (locked) 0.25f else 1f
        }
    }

    private fun itemsLabel(count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100
        return when {
            mod10 == 1 && mod100 != 11 -> "$count artikal"
            mod10 in 2..4 && mod100 !in 12..14 -> "$count artikla"
            else -> "$count artikala"
        }
    }

    /** Mirrors composeApp's print FAB DropdownMenu - a white, rounded popup anchored to
     * [anchor] with three pill-shaped actions, shown only while the order is pending. */
    private fun showPrintMenu(anchor: View) {
        val menuView = layoutInflater.inflate(R.layout.popup_order_print_menu, null)
        val popup = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = 8f
        }

        menuView.findViewById<TextView>(R.id.action_print_order).setOnClickListener {
            popup.dismiss()
            viewModel.printOrder()
        }
        menuView.findViewById<TextView>(R.id.action_print_estimate).setOnClickListener {
            popup.dismiss()
            showSnack(R.string.order_detail_coming_soon, KSnackbar.Type.WARNING)
        }
        menuView.findViewById<TextView>(R.id.action_send_estimate).setOnClickListener {
            popup.dismiss()
            showSnack(R.string.order_detail_coming_soon, KSnackbar.Type.WARNING)
        }

        menuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val xOffset = anchor.width - menuView.measuredWidth
        val yOffset = -(anchor.height + menuView.measuredHeight + dp(8))
        popup.showAsDropDown(anchor, xOffset, yOffset)
    }

    private fun renderDeliveryExpanded() {
        binding.layoutDeliveryExpanded.visibility = if (deliveryExpanded) View.VISIBLE else View.GONE
        binding.iconDeliveryChevron.setImageResource(
            if (deliveryExpanded) R.drawable.ic_k_ord_chevron_up else R.drawable.ic_k_ord_chevron_down
        )
    }

    private fun renderProviderSelection() {
        binding.rowProviderA2b.setBackgroundResource(
            if (selectedCarrierCode == "A2B") R.drawable.bg_k_ord_provider_selected else android.R.color.transparent
        )
        binding.rowProviderExpress.setBackgroundResource(
            if (selectedCarrierCode == "EURO_EXPRESS") R.drawable.bg_k_ord_provider_selected else android.R.color.transparent
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

    /** Mirrors composeApp's Specification/SpecificationItem - one row per item, dividers between. */
    private fun renderSpecification(products: List<VendorProduct>, canEdit: Boolean) {
        binding.textNoItems.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        val container = binding.specItemsContainer
        container.removeAllViews()
        products.forEachIndexed { index, product ->
            if (index > 0) container.addView(buildHorizontalDivider())
            container.addView(buildSpecItem(product, canEdit))
        }
    }

    private fun buildSpecItem(product: VendorProduct, editable: Boolean): View {
        val context = requireContext()
        val item = ItemOrderSpecItemBinding.inflate(layoutInflater, binding.specItemsContainer, false)

        val price = product.price?.toDoubleOrNull() ?: 0.0
        val discountPercent = product.rabat().toIntOrNull() ?: 0
        val discountedPrice = price * (1.0 - discountPercent / 100.0)
        val qty = product.qtyOrdered?.toIntOrNull() ?: 0
        val rowTotalVpc = discountedPrice * qty
        val rowTotalWithPdv = rowTotalVpc * 1.17
        // Rabat also eats into Karika's commission on this row - e.g. a 50% rabat halves it.
        val commissionAmount = (product.commission?.toDoubleOrNull() ?: 0.0) * (1.0 - discountPercent / 100.0)

        val image = product.imagePath()?.let { HttpClientProvider.imageUrl(it) }
        item.textImagePlaceholder.visibility = if (image == null) View.VISIBLE else View.GONE
        item.imageProduct.visibility = if (image == null) View.GONE else View.VISIBLE
        if (image != null) item.imageProduct.loadUrl(image, viewLifecycleOwner)

        item.textName.text = product.name ?: "—"
        item.textQtyLine.text = SpannableStringBuilder().apply {
            val originalQty = product.originalQty()
            if (originalQty.isNotEmpty()) {
                val start = length
                append(originalQty)
                setSpan(StrikethroughSpan(), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(context.getColor(R.color.k_subtle)), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(" ")
            }
            append(product.qty())
            append(" × ")
            append(karikaPriceFormat(discountedPrice) + " KM")
            append(" · rabat ")
            append(product.rabat() + "%")
        }
        item.textPdvLine.text = getString(
            R.string.order_detail_row_with_pdv_format,
            karikaPriceFormat(rowTotalWithPdv),
            product.commissionPercentLabel(),
            karikaPriceFormat(commissionAmount)
        )
        item.textRowTotal.text = karikaPriceFormat(rowTotalVpc) + " KM"
        item.buttonEdit.visibility = if (editable) View.VISIBLE else View.GONE
        item.buttonEdit.setOnClickListener { openEditItemSheet(product) }
        return item.root
    }

    private fun buildHorizontalDivider(): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
        setBackgroundColor(requireContext().getColor(R.color.k_line))
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
        binding.commentsContainer.setPadding(0, 0, 0, if (comments.isNotEmpty()) dp(2) else 0)
        val customerName = viewModel.vendorOrder.value?.b2bPravnoLice
        comments.forEach { comment -> binding.commentsContainer.addView(buildCommentView(comment, customerName)) }
    }

    /** Mirrors composeApp's CommentBubble. */
    private fun buildCommentView(comment: Comment, customerName: String?): View {
        val item = ItemOrderCommentBinding.inflate(layoutInflater, binding.commentsContainer, false)
        val context = requireContext()
        if (comment.isMine()) {
            item.layoutMine.visibility = View.VISIBLE
            item.textMineMessage.visibility = if (comment.message().isBlank()) View.GONE else View.VISIBLE
            item.textMineMessage.setHtmlText(comment.message())
            item.textMineTime.text = comment.createdAt()
            comment.files.orEmpty().forEach { addCommentFile(item.layoutMineFiles, it, context.getColor(R.color.k_white)) }
        } else {
            item.layoutOther.visibility = View.VISIBLE
            item.textOtherInitials.text = customerName.messageInitials()
            item.textOtherMessage.visibility = if (comment.message().isBlank()) View.GONE else View.VISIBLE
            item.textOtherMessage.setHtmlText(comment.message())
            item.textOtherFooter.text = listOfNotNull(
                customerName?.takeIf { it.isNotBlank() },
                comment.createdAt().takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            comment.files.orEmpty().forEach { addCommentFile(item.layoutOtherFiles, it, context.getColor(R.color.k_accent)) }
        }
        return item.root
    }

    /** CommentFileAttachment: images as a tappable preview, anything else as a PDF row. */
    private fun addCommentFile(container: LinearLayout, file: CommentFile, color: Int) {
        val path = file.url?.takeIf { it.isNotBlank() } ?: return
        val url = HttpClientProvider.imageUrl(path)
        val context = requireContext()
        if ((file.name?.takeIf { it.isNotBlank() } ?: path).isImageFile()) {
            container.addView(com.google.android.material.card.MaterialCardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(200), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(8)
                }
                radius = dp(10).toFloat()
                cardElevation = 0f
                strokeWidth = 0
                addView(ImageView(context).apply {
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    loadUrl(url, viewLifecycleOwner)
                })
                setOnClickListener {
                    ImagePreviewFragment.newInstance(url).show(childFragmentManager, "image_preview")
                }
            })
        } else {
            container.addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
                addView(ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
                    setImageResource(R.drawable.ic_pdf)
                    imageTintList = ColorStateList.valueOf(color)
                })
                addView(TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = dp(8) }
                    text = file.name.orEmpty()
                    setTextColor(color)
                    textSize = 12f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                })
                setOnClickListener { PdfViewerActivity.open(context, url, file.name) }
            })
        }
    }

    private fun String.isImageFile() = lowercase().let {
        it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
            it.endsWith(".gif") || it.endsWith(".webp")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
