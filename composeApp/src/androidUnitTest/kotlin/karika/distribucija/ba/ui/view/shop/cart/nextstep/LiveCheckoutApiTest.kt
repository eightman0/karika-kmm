package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Places a real order as the customer test account on the flavor's backend, through the real
 * ShippingDetailsView and DefaultShippingDetailsComponent, then cancels it again so it never
 * reaches the vendor. It only runs when the account's cart is empty to begin with, because
 * an order takes the whole cart.
 */
class LiveCheckoutApiTest : LiveShopTest() {

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
        testOrder = orderId to product.vendorId()
        assertTrue(orderId.isNotBlank(), "empty order id")
        assertFalse('"' in success.orderId, "the order number is shown with quotes: ${success.orderId}")

        // The order took the cart; the app starts a new, empty one
        waitForServer { currentCart().items.none { it.sku == product.sku } }

        val cancelled = cancelTestOrder()
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
        const val VENDOR_NOTE = "Automatski UI test, narudzba se odmah otkazuje"
    }
}
