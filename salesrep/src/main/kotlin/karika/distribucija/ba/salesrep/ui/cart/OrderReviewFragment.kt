package karika.distribucija.ba.salesrep.ui.cart

import karika.distribucija.ba.salesrep.util.KSnackbar
import karika.distribucija.ba.salesrep.util.showSnack
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderReviewBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderReviewSpecBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem
import karika.distribucija.ba.salesrep.model.VendorDeliveryServiceData
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.ui.messages.messageInitials
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

        // Same two warnings as Compose's bottom panel (no capability is only guarded in confirm).
        binding.textWarning.apply {
            when {
                !viewModel.customerActive -> { text = getString(R.string.k_cart2_warning_inactive); visibility = View.VISIBLE }
                !viewModel.hasShippingAddress -> { text = getString(R.string.k_cart2_warning_no_shipping); visibility = View.VISIBLE }
                else -> visibility = View.GONE
            }
        }
        updateConfirmButtonState(isPlacingOrder = false)
        renderCustomerInfo()
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
            AnalyticsTracker.trackClick("order_review", "confirm_order")
            viewModel.confirmOrder(binding.editNote.text?.toString().orEmpty(), buildShippingFormIfComplete())
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            val items = cart?.items.orEmpty()
            renderSpecificationItems(items)

            // Per the cart response: grand_total is the post-discount VPC total, subtotal the
            // pre-discount one, total_with_tax the final total - same mapping as Compose.
            val vpcTotal = karikaPriceFormat(cart?.grandTotal ?: 0.0) + " KM"
            val totalWithTax = karikaPriceFormat(cart?.totalWithTax ?: 0.0) + " KM"
            binding.textItemsCount.text = (cart?.itemsCount ?: items.size).toString()
            binding.textTotalVpc.text = vpcTotal
            binding.textReviewSubtotal.text = karikaPriceFormat(cart?.subtotal ?: 0.0) + " KM"
            binding.textReviewDiscount.text = "-" + karikaPriceFormat(cart?.discountAmount ?: 0.0) + " KM"
            binding.textReviewTotalVpc.text = vpcTotal
            binding.textReviewPdv.text = karikaPriceFormat(cart?.totalTax ?: 0.0) + " KM"
            binding.textReviewCommission.text = karikaPriceFormat(cart?.fee ?: 0.0) + " KM"
            binding.textReviewTotalWithTax.text = totalWithTax
            binding.textBottomTotal.text = totalWithTax

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
            if (message != null) showSnack(message)
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
                showSnack(message, KSnackbar.Type.SUCCESS)

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
                        popUpTo(R.id.analyticsOverviewFragment) { inclusive = false }
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
        binding.textDeliverySubtitle.setText(
            if (deliveryExpanded) R.string.k_cart2_delivery_subtitle_expanded else R.string.k_ord_delivery_subtitle
        )
        binding.iconDeliveryChevron.rotation = if (deliveryExpanded) 180f else 0f
    }

    /** Courier option: blue border and a filled blue check circle when selected. */
    private fun renderProviderSelection() {
        listOf(
            Triple(binding.rowProviderA2b, binding.radioA2b, binding.checkA2b) to "A2B",
            Triple(binding.rowProviderExpress, binding.radioExpress, binding.checkExpress) to "EURO_EXPRESS"
        ).forEach { (views, code) ->
            val (row, radio, check) = views
            val selected = selectedCarrierCode == code
            row.setBackgroundResource(if (selected) R.drawable.bg_k_cart2_provider_selected else R.drawable.bg_k_cart2_provider)
            radio.setBackgroundResource(if (selected) R.drawable.bg_k_cart2_radio_on else R.drawable.bg_k_cart2_radio_off)
            check.visibility = if (selected) View.VISIBLE else View.GONE
        }
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

    /** One SpecificationItem card per line, 10dp apart. VPC/PDV values are derived from
     * price/discount/qty locally rather than the server's row_total, same as Compose. */
    private fun renderSpecificationItems(items: List<OnBehalfCartResponseItem>) {
        val container = binding.layoutSpecItems
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        items.forEachIndexed { index, item ->
            val card = ItemOrderReviewSpecBinding.inflate(inflater, container, false)
            val discountPercent = item.discountPercent ?: 0
            val discountedPrice = item.price * (1.0 - discountPercent / 100.0)
            val rowTotalVpc = discountedPrice * item.qty
            val rowTotalWithPdv = rowTotalVpc * 1.17

            card.textName.text = item.name
            card.textDiscountPill.text = getString(R.string.k_cart2_spec_discount_pill, discountPercent)
            card.textCommissionPill.text = getString(R.string.k_cart2_spec_commission_pill, item.commissionPercent.toInt())
            card.textPrice.text = karikaPriceFormat(discountedPrice) + " KM"
            card.textQty.text = getString(
                R.string.k_cart2_spec_qty_format,
                item.qty,
                item.quantityUnit ?: getString(R.string.k_cart2_default_unit)
            )
            card.textTotalVpc.text = karikaPriceFormat(rowTotalVpc) + " KM"
            card.textTotalWithPdv.text = karikaPriceFormat(rowTotalWithPdv) + " KM"
            card.textCommission.text = karikaPriceFormat(item.commission) + " KM"
            card.buttonEdit.setOnClickListener { openEditItemSheet(item) }

            if (index > 0) {
                (card.root.layoutParams as LinearLayout.LayoutParams).topMargin =
                    (10 * resources.displayMetrics.density).toInt()
            }
            container.addView(card.root)
        }
    }

    private fun openEditItemSheet(item: OnBehalfCartResponseItem) {
        EditCartItemBottomSheet(
            item = item,
            canDiscount = viewModel.canCreateDiscountFor
        ) { newQty, newDiscountPercent ->
            viewModel.updateItem(item, newQty, newDiscountPercent)
        }.show(parentFragmentManager, "EditCartItemBottomSheet")
    }

    /** Same `canConfirm` as Compose: items, an active partnership and a default shipping
     * address; while placing, the button keeps its full color and shows a spinner. */
    private fun updateConfirmButtonState(isPlacingOrder: Boolean) {
        val hasItems = CartState.cart.value?.isEmpty == false
        val canConfirm = hasItems && !isPlacingOrder &&
            viewModel.customerActive && viewModel.hasShippingAddress
        binding.buttonConfirm.isEnabled = canConfirm
        binding.buttonConfirm.setBackgroundResource(
            if (canConfirm || isPlacingOrder) R.drawable.bg_k_button_primary else R.drawable.bg_k_ord_button_disabled
        )
        binding.textConfirm.visibility = if (isPlacingOrder) View.GONE else View.VISIBLE
        binding.progressConfirm.visibility = if (isPlacingOrder) View.VISIBLE else View.GONE

        binding.buttonBack.isEnabled = !isPlacingOrder
        binding.editNote.isEnabled = !isPlacingOrder
    }

    /** "Informacije o narudžbi" card: initials, company (person's name when there is none),
     * email and the PartnershipBadge colors/labels from Compose. */
    private fun renderCustomerInfo() {
        val customerName = requireArguments().getString("customerName").orEmpty()
        val label = viewModel.customerCompany?.takeIf { it.isNotBlank() } ?: customerName
        binding.textCustomerInitials.text = label.messageInitials()
        binding.textCustomerCompany.text = label
        binding.textCustomerEmail.text =
            viewModel.customerEmail?.takeIf { it.isNotBlank() } ?: getString(R.string.order_detail_dash)

        val status = viewModel.partnershipStatus
        binding.textPartnershipBadge.text = when (status) {
            "active" -> getString(R.string.review_badge_active)
            "pending" -> getString(R.string.review_badge_pending)
            "revoked" -> getString(R.string.review_badge_revoked)
            "rejected" -> getString(R.string.review_badge_rejected)
            else -> status
        }
        val (badgeBgRes, badgeColorRes) = when {
            viewModel.customerActive -> R.drawable.bg_k_pill_green_soft to R.color.k_green
            status == "pending" -> R.drawable.bg_k_pill_amber_soft to R.color.k_amber
            status == "rejected" -> R.drawable.bg_k_pill_red_soft to R.color.k_red
            status == "revoked" -> R.drawable.bg_k_pill_field to R.color.k_muted
            else -> R.drawable.bg_k_pill_accent_soft to R.color.k_accent
        }
        binding.textPartnershipBadge.setBackgroundResource(badgeBgRes)
        binding.textPartnershipBadge.setTextColor(requireContext().getColor(badgeColorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
