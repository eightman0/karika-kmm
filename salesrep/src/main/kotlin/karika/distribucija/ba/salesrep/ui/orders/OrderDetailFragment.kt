package karika.distribucija.ba.salesrep.ui.orders

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderDetailBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderCommentBinding
import karika.distribucija.ba.salesrep.databinding.ItemOrderDetailProductBinding
import karika.distribucija.ba.salesrep.model.Comment
import karika.distribucija.ba.salesrep.model.VendorOrder
import karika.distribucija.ba.salesrep.model.VendorProduct
import karika.distribucija.ba.salesrep.util.karikaPriceFormat

/** Mirrors composeApp's ui/view/salesrep/orders/detail/SalesOrderDetailView.kt. */
class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderDetailViewModel by viewModels()

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

        binding.buttonSendComment.setOnClickListener {
            val text = binding.editComment.text?.toString().orEmpty()
            if (text.isBlank()) return@setOnClickListener
            viewModel.sendComment(text)
            binding.editComment.setText("")
        }

        binding.buttonPrint.setOnClickListener { viewModel.printOrder() }

        viewModel.vendorOrder.observe(viewLifecycleOwner) { order -> renderOrder(order) }
        viewModel.comments.observe(viewLifecycleOwner) { comments -> renderComments(comments) }

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
        binding.textItemsCount.text = getString(R.string.order_detail_items_count_format, products.size)
        binding.textNoItems.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        binding.productsContainer.removeAllViews()
        products.forEach { product -> binding.productsContainer.addView(buildProductView(product)) }

        binding.textSummaryVpc.text = karikaPriceFormat(vpcTotal) + " KM"
        binding.textSummaryPdv.text = karikaPriceFormat(pdvTotal) + " KM"
        binding.textSummaryTotal.text = karikaPriceFormat(grandTotal) + " KM"
        binding.textSummaryCommission.text = commissionText
        binding.textSummaryGrandTotal.text = karikaPriceFormat(grandTotal) + " KM"
    }

    private fun buildProductView(product: VendorProduct): View {
        val itemBinding = ItemOrderDetailProductBinding.inflate(layoutInflater, binding.productsContainer, false)
        val dash = getString(R.string.order_detail_dash)

        itemBinding.textProductName.text = product.name ?: dash
        itemBinding.textProductQty.text = getString(
            R.string.order_detail_qty_format,
            product.qtyOrdered ?: dash,
            product.unit ?: "kom"
        )
        itemBinding.textProductRabat.text = getString(R.string.order_detail_rabat_format, product.rabat())
        itemBinding.textPriceVpcValue.text = product.priceVpc()
        itemBinding.textTotalVpcValue.text = product.totalVpc()
        itemBinding.textTotalPdvValue.text = product.totalWithPdv()
        itemBinding.textCommissionLabel.text =
            getString(R.string.order_detail_commission_format, product.commissionPercent ?: dash)
        itemBinding.textCommissionValue.text = if (product.commission != null) {
            karikaPriceFormat(product.commission.toDoubleOrNull() ?: 0.0) + " KM"
        } else {
            dash
        }
        return itemBinding.root
    }

    private fun renderComments(comments: List<Comment>) {
        binding.commentsContainer.removeAllViews()
        comments.forEach { comment -> binding.commentsContainer.addView(buildCommentView(comment)) }
        binding.commentsDivider.visibility = if (comments.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun buildCommentView(comment: Comment): View {
        val itemBinding = ItemOrderCommentBinding.inflate(layoutInflater, binding.commentsContainer, false)
        val isMine = comment.isMine()

        itemBinding.textCommentMessage.text = comment.message()
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
