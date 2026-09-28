package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.cart.DefaultCartComponent
import karika.distribucija.ba.ui.view.shop.cart.orderValid
import kotlin.math.ceil
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Places a real order as the customer test account on the flavor's backend, through the real
 * ShippingDetailsView and DefaultShippingDetailsComponent, then cancels it again so it never
 * reaches the vendor. It only runs when the account's cart is empty to begin with, because
 * an order takes the whole cart.
 */
class LiveCheckoutApiTest : LiveShopTest() {

    private var placedOrder: Pair<String, String?>? = null

    @After
    fun cancelLeftoverOrder() {
        // A test that failed after placing the order still takes it back
        placedOrder?.let { (orderId, vendorId) -> cancel(orderId, vendorId) }
    }

    private fun cancel(orderId: String, vendorId: String?): ResultState<Boolean> {
        val result = runBlocking { OrdersRepository().cancel(orderId, vendorId, CANCEL_REASON).last() }
        if (result is ResultState.Success) {
            placedOrder = null
        }
        return result
    }

    private fun loadCustomer() {
        stateHolder.customerSpecificHandler.getUserDetails()
        waitForServer { stateHolder.customerSpecificHandler.userDetails.value.id != null }
    }

    /**
     * Fills the empty cart with one recommended product, in steps of its minimum quantity until
     * the vendor's minimum order is reached, and returns it; skips products whose stock or a
     * sensible quantity does not reach the minimum.
     */
    private fun fillOrderableCart(): Product {
        val cartComponent = DefaultCartComponent(componentContext(), stateHolder)
        for (product in recommendedProducts().filter { it.hasOnStock() }) {
            cartComponent.addToCart(product, product.minQty(), showSnack = false)
            waitForServer { currentCart().items.any { it.sku == product.sku } }
            val itemId = currentCart().items.first { it.sku == product.sku }.itemId
            stateHolder.cartHandler.reloadCart()
            waitForServer { cartHolds(product) }

            val vendor = stateHolder.cartHandler.cart.value.items.keys.single()
            val minimum = vendor.minOrderAmount()?.toDoubleOrNull() ?: 0.0
            val stepPrice = product.currentPrice() * 1.17 * product.minQty()
            val steps = if (stepPrice > 0) ceil(minimum / stepPrice).toInt().coerceAtLeast(1) else 1
            val qty = steps * product.minQty()
            val stock = product.stockData?.salableQty ?: Long.MAX_VALUE

            if (steps <= MAX_STEPS && qty <= stock) {
                if (qty > product.minQty()) {
                    val inCart = stateHolder.cartHandler.cart.value.items.values.flatten()
                        .first { it.first.sku == product.sku }.first
                    cartComponent.updateCart(inCart, qty)
                    waitForServer { currentCart().items.any { it.sku == product.sku && it.qty == qty } }
                    stateHolder.cartHandler.reloadCart()
                    waitForServer {
                        stateHolder.cartHandler.cart.value.items.values.flatten()
                            .any { it.first.sku == product.sku && it.second == qty }
                    }
                }
                if (stateHolder.cartHandler.cart.value.items.orderValid()) {
                    return product
                }
            }
            itemId?.let { runBlocking { CartRepository().removeFromCart(it.toString()).last() } }
            stateHolder.cartHandler.reloadCart()
            waitForServer { stateHolder.cartHandler.cart.value.items.isEmpty() }
        }
        assumeTrue("no recommended product can reach its vendor's minimum order", false)
        error("unreachable")
    }

    private fun cartHolds(product: Product) =
        stateHolder.cartHandler.cart.value.items.values.flatten().any { it.first.sku == product.sku }

    @Test
    fun orderIsPlacedWithTheDefaultAddressAndCancelled() {
        useCustomerCart()
        assumeTrue("the test account's cart is not empty", currentCart().items.isEmpty())
        loadCustomer()
        val product = fillOrderableCart()

        val component = DefaultShippingDetailsComponent(componentContext(), stateHolder)
        assumeTrue("the test account has no shipping address", component.addresses.value.isNotEmpty())
        compose.setContent { ShippingDetailsView(component) }

        val chosen = component.addresses.value.firstOrNull { it.id?.toString() == component.selectedAddress.value }
            ?: component.addresses.value.first().also {
                compose.onNodeWithTag(ShippingTestTags.address(it.id)).performScrollTo().performClick()
            }
        compose.onNodeWithTag(ShippingTestTags.VENDOR_NOTE).performScrollTo().performTextInput(VENDOR_NOTE)
        dismissSnackbar()
        compose.onNodeWithText("Završi narudžbu").performClick()
        waitForServer { openedInShop is MainConfig.CartSuccess || snackbarMessage() != null }

        val success = assertIs<MainConfig.CartSuccess>(
            openedInShop,
            "no order for ${product.sku} to address ${chosen.id}, message: ${snackbarMessage()}"
        )
        val orderId = success.orderId.trim('"')
        placedOrder = orderId to product.vendorId()
        assertTrue(orderId.isNotBlank(), "empty order id")
        assertFalse('"' in success.orderId, "the order number is shown with quotes: ${success.orderId}")

        // The order took the cart; the app starts a new, empty one
        waitForServer { currentCart().items.none { it.sku == product.sku } }

        val cancelled = cancel(orderId, product.vendorId())
        assertIs<ResultState.Success<*>>(cancelled, "order $orderId was placed but could not be cancelled: $cancelled")
    }

    @Test
    fun finishingWithoutAnAddressAsksForOne() {
        loadCustomer()
        val component = DefaultShippingDetailsComponent(componentContext(), stateHolder)
        compose.setContent { ShippingDetailsView(component) }

        component.selectedAddress.value = ""
        dismissSnackbar()
        compose.onNodeWithText("Završi narudžbu").performClick()
        waitForServer { snackbarMessage() != null }

        assertEquals("Odaberite adresu za dostavu.", snackbarMessage())
        assertNull(openedInShop)
    }

    private companion object {
        /** More than this many minimum quantities for one vendor minimum is not a sensible test order. */
        const val MAX_STEPS = 20
        const val VENDOR_NOTE = "Automatski UI test, narudzba se odmah otkazuje"
        const val CANCEL_REASON = "Automatski UI test"
    }
}
