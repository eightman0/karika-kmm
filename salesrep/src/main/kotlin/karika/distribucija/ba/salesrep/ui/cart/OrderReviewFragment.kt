package karika.distribucija.ba.salesrep.ui.cart

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderReviewBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat

/** Mirrors composeApp's SalesOrderReviewView.kt. */
class OrderReviewFragment : Fragment() {

    private var _binding: FragmentOrderReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderReviewViewModel by viewModels()

    private var deliveryExpanded = false
    private var selectedCarrierCode = ""
    private var shippingCostA2b: Double? = null
    private var shippingCostExpress: Double? = null
    private var defaultsApplied = false

    private val specColumnWidthsDp = intArrayOf(110, 85, 105, 95, 115, 140, 170, 100, 90)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customerName = requireArguments().getString("customerName").orEmpty()
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.review_title)
        binding.scrollContent.applyImeBottomPadding()

        binding.textWarning.apply {
            when (viewModel.ineligibleReason) {
                "no_capability" -> { text = getString(R.string.review_warning_no_capability); visibility = View.VISIBLE }
                "inactive" -> { text = getString(R.string.review_warning_inactive); visibility = View.VISIBLE }
                "no_shipping" -> { text = getString(R.string.review_warning_no_shipping); visibility = View.VISIBLE }
                else -> visibility = View.GONE
            }
        }
        updateConfirmButtonState(isPlacingOrder = false)
        renderCustomerInfo()
        renderSpecificationHeader()
        renderDeliveryExpanded()
        renderProviderSelection()
        renderProviderPrices()

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

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonConfirm.setOnClickListener {
            viewModel.confirmOrder(binding.editNote.text?.toString().orEmpty(), buildShippingFormIfComplete())
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            val items = cart?.items.orEmpty()
            renderSpecificationRows(items)

            val vpcTotal = cart?.grandTotal ?: 0.0
            binding.textItemsCount.text = (cart?.itemsCount ?: items.size).toString()
            binding.textTotalVpc.text = cart?.grandTotalString() ?: "0,00 KM"
            binding.textReviewSubtotal.text = cart?.subtotalString() ?: "0,00 KM"
            binding.textReviewPdv.text = karikaPriceFormat(cart?.totalTax ?: 0.0) + " KM"
            binding.textReviewDiscount.text = "-" + (cart?.discountString() ?: "0,00 KM")
            binding.textReviewTotalWithTax.text = karikaPriceFormat(cart?.totalWithTax ?: 0.0) + " KM"
            binding.textReviewCommission.text = karikaPriceFormat(cart?.fee ?: 0.0) + " KM"

            binding.textStatVpc.text = karikaPriceFormat(vpcTotal) + " KM"
            binding.textStatPdv.text = karikaPriceFormat(cart?.totalTax ?: 0.0) + " KM"
            binding.textStatTotal.text = karikaPriceFormat(cart?.totalWithTax ?: 0.0) + " KM"
            binding.textStatCommission.text = karikaPriceFormat(cart?.fee ?: 0.0) + " KM"

            updateConfirmButtonState(isPlacingOrder = viewModel.isPlacingOrder.value == true)
        }

        viewModel.shippingDefaults.observe(viewLifecycleOwner) { defaults ->
            if (defaults != null && !defaultsApplied) {
                defaultsApplied = true
                binding.editContactName.setText(defaults.contactName)
                binding.editContactEmail.setText(defaults.email)
                binding.editContactPhone.setText(defaults.telephone)
                binding.editCity.setText(defaults.city)
                binding.editDeliveryAddress.setText(defaults.street)
                binding.editPostalCode.setText(defaults.postcode)
                binding.editPackageWidth.setText(defaults.packageWidth)
                binding.editPackageHeight.setText(defaults.packageHeight)
                binding.editPackageDepth.setText(defaults.packageDepth)
                binding.editPackageWeight.setText(defaults.packageWeight)
                binding.editDeliveryNote.setText(defaults.note)
                selectedCarrierCode = defaults.shippingCompany.orEmpty()
                renderProviderSelection()
            }
        }

        viewModel.isPlacingOrder.observe(viewLifecycleOwner) { saving ->
            updateConfirmButtonState(isPlacingOrder = saving == true)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.shouldGoBack.observe(viewLifecycleOwner) { shouldGoBack ->
            if (shouldGoBack) findNavController().popBackStack()
        }

        viewModel.orderPlaced.observe(viewLifecycleOwner) { order ->
            if (order != null) {
                val message = if (order.status == "pending") {
                    "Narudžba ${order.incrementId} je poslana menadžeru na odobrenje."
                } else {
                    "Narudžba ${order.incrementId} uspješno kreirana!"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                findNavController().navigate(
                    R.id.orderDetailFragment,
                    bundleOf(
                        "orderId" to order.orderId,
                        "incrementId" to order.incrementId,
                        "customerId" to viewModel.customerId,
                        "customerName" to customerName,
                        "grandTotal" to order.grandTotal.toFloat(),
                        "status" to order.status,
                        "createdAt" to null
                    ),
                    androidx.navigation.navOptions {
                        popUpTo(R.id.ordersListFragment) { inclusive = false }
                    }
                )
            }
        }
    }

    /** Matches Compose's inline `shippingComplete` check in the Confirm button's onClick - all
     * fields (including the carrier choice) must be filled in, else the shipping form is
     * dropped entirely rather than submitted partially. */
    private fun buildShippingFormIfComplete(): VendorDeliveryServiceData? {
        val name = binding.editContactName.text?.toString().orEmpty()
        val email = binding.editContactEmail.text?.toString().orEmpty()
        val phone = binding.editContactPhone.text?.toString().orEmpty()
        val city = binding.editCity.text?.toString().orEmpty()
        val address = binding.editDeliveryAddress.text?.toString().orEmpty()
        val postalCode = binding.editPostalCode.text?.toString().orEmpty()
        val weight = binding.editPackageWeight.text?.toString().orEmpty()
        val width = binding.editPackageWidth.text?.toString().orEmpty()
        val height = binding.editPackageHeight.text?.toString().orEmpty()
        val depth = binding.editPackageDepth.text?.toString().orEmpty()

        val shippingComplete = selectedCarrierCode.isNotBlank() &&
            name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
            city.isNotBlank() && address.isNotBlank() && postalCode.isNotBlank() &&
            weight.isNotBlank() && width.isNotBlank() && height.isNotBlank() && depth.isNotBlank()

        if (!shippingComplete) return null
        return VendorDeliveryServiceData(
            name = name,
            email = email,
            telephone = phone,
            city = city,
            street = address,
            postcode = postalCode,
            weight = weight,
            width = width,
            height = height,
            depth = depth,
            note = binding.editDeliveryNote.text?.toString().orEmpty(),
            companyCode = selectedCarrierCode
        )
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

    private fun renderSpecificationHeader() {
        binding.rowSpecHeader.removeAllViews()
        val headers = listOf(
            R.string.review_spec_header_item,
            R.string.review_spec_header_discount,
            R.string.review_spec_header_price,
            R.string.review_spec_header_qty,
            R.string.review_spec_header_total_vpc,
            R.string.review_spec_header_total_with_tax,
            R.string.review_spec_header_commission_percent,
            R.string.review_spec_header_commission,
            R.string.review_spec_header_actions
        )
        headers.forEachIndexed { index, res ->
            binding.rowSpecHeader.addView(
                buildSpecCell(getString(res), specColumnWidthsDp[index], color = R.color.karika_gray6, bold = true, sizeSp = 10f)
            )
            if (index != headers.lastIndex) binding.rowSpecHeader.addView(buildVerticalDivider())
        }
    }

    private fun renderSpecificationRows(items: List<OnBehalfCartResponseItem>) {
        binding.tableRowsContainer.removeAllViews()
        items.forEachIndexed { rowIndex, item ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val cells = listOf(
                item.name,
                "${item.discountPercent ?: 0}",
                item.priceString(),
                "${item.qty} ${item.quantityUnit ?: getString(R.string.review_spec_default_qty_unit)}",
                karikaPriceFormat(item.price * item.qty) + " KM",
                item.rowTotalString(),
                "${item.commissionPercent.toInt()}",
                karikaPriceFormat(item.commission) + " KM"
            )
            cells.forEachIndexed { colIndex, text ->
                row.addView(buildSpecCell(text, specColumnWidthsDp[colIndex], bold = colIndex == 0))
                row.addView(buildVerticalDivider())
            }
            row.addView(buildEditActionCell(specColumnWidthsDp[8]) { openEditItemSheet(item) })
            binding.tableRowsContainer.addView(row)
            if (rowIndex != items.lastIndex) {
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

    private fun openEditItemSheet(item: OnBehalfCartResponseItem) {
        EditCartItemBottomSheet(
            item = item,
            canDiscount = viewModel.canCreateDiscountFor
        ) { newQty, newDiscountPercent ->
            viewModel.updateItem(item, newQty, newDiscountPercent)
        }.show(parentFragmentManager, "EditCartItemBottomSheet")
    }

    /** Blocks the rest of the form and shows an in-button spinner while the order is being
     * placed, mirroring Compose's CircularProgressIndicator-inside-the-button pattern. Also
     * matches Compose's `canConfirm` requiring a non-empty cart - relevant if the rep empties
     * the cart via the specification table's edit sheet without leaving this screen. */
    private fun updateConfirmButtonState(isPlacingOrder: Boolean) {
        val hasItems = CartState.cart.value?.isEmpty == false
        val canConfirm = viewModel.isEligible && hasItems && !isPlacingOrder
        binding.buttonConfirm.isEnabled = canConfirm
        binding.buttonConfirm.setBackgroundResource(
            if (canConfirm) R.drawable.bg_fab_order_customer else R.drawable.bg_button_filled_disabled
        )
        binding.textConfirm.visibility = if (isPlacingOrder) View.GONE else View.VISIBLE
        binding.progressConfirm.visibility = if (isPlacingOrder) View.VISIBLE else View.GONE

        binding.buttonBack.isEnabled = !isPlacingOrder
        binding.editNote.isEnabled = !isPlacingOrder
    }

    /** Mirrors the Compose Review screen's "Informacije o narudžbi" card: customer company/email,
     * a two-state (active/not-active) partnership badge, and the items count. */
    private fun renderCustomerInfo() {
        val customerName = requireArguments().getString("customerName").orEmpty()
        binding.textCustomerCompany.text = viewModel.customerCompany?.takeIf { it.isNotBlank() } ?: customerName
        binding.textCustomerEmail.text =
            viewModel.customerEmail?.takeIf { it.isNotBlank() } ?: getString(R.string.order_detail_dash)

        val badgeLabel = when (viewModel.partnershipStatus) {
            "active" -> R.string.review_badge_active
            "pending" -> R.string.review_badge_pending
            "rejected" -> R.string.review_badge_rejected
            "revoked" -> R.string.review_badge_revoked
            else -> R.string.review_badge_pending
        }
        binding.textPartnershipBadge.text = getString(badgeLabel)
        val (badgeBgRes, badgeColorRes) = if (viewModel.customerActive) {
            R.drawable.bg_review_badge_active to R.color.karika_green3
        } else {
            R.drawable.bg_review_badge_pending to R.color.karika_blue
        }
        binding.textPartnershipBadge.setBackgroundResource(badgeBgRes)
        binding.textPartnershipBadge.setTextColor(requireContext().getColor(badgeColorRes))
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics
    ).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
