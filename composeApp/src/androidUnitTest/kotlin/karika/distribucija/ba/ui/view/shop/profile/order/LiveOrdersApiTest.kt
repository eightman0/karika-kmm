package karika.distribucija.ba.ui.view.shop.profile.order

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.profile.order.details.OrderDetailsComponent
import karika.distribucija.ba.ui.view.shop.profile.order.details.OrderDetailsView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import org.junit.Test

/**
 * "Moje narudžbe" of the customer test account against the real backend, through the real
 * OrdersView, OrderDetailsView and their components. Each test places its own order (only
 * when the account's cart is empty) and cancels it, through the screen or afterwards.
 */
class LiveOrdersApiTest : LiveShopTest() {

    private lateinit var component: OrdersComponent

    /** Places a test order and waits for it in the order list, shown when [show]. */
    private fun ordersWithTestOrder(show: Boolean = true): Pair<OrdersResponse, Product> {
        val product = placeTestOrder()
        val orderId = testOrder!!.first
        component = OrdersComponent(componentContext(), stateHolder)
        if (show) {
            compose.setContent { OrdersView(component) }
        } else {
            component.loadNextPage(true)
        }
        waitForServer { component.orders.value.any { it.orderId == orderId } }
        return component.orders.value.first { it.orderId == orderId } to product
    }

    private fun OrdersResponse.vendorOrder(): Order = orders.first()

    @Test
    fun placedOrderIsListedAsPending() {
        val (order, _) = ordersWithTestOrder()

        compose.onAllNodesWithText("#${order.incrementId}").onFirst().performScrollTo()
        assertEquals("pending", order.vendorOrder().status)
        compose.onNode(hasText("Na čekanju") and hasAnyAncestor(hasTestTag(orderVendorTag(order.vendorOrder()))))
            .assertExists()
    }

    @Test
    fun orderIsCancelledFromTheList() {
        val (order, _) = ordersWithTestOrder()
        val vendorOrder = order.vendorOrder()
        val inOrder = hasAnyAncestor(hasTestTag(orderVendorTag(vendorOrder)))

        compose.onNode(hasContentDescription("Prikaži opcije") and inOrder).performScrollTo().performClick()
        compose.onNode(hasText("Otkaži narudžbu") and inOrder).performScrollTo().performClick()
        compose.onNodeWithText("Otkazivanje narudžbe").assertExists()
        compose.onNodeWithText("Ostalo").performClick()
        compose.onNodeWithText("Potvrdi").performClick()
        waitForServer {
            component.orders.value.first { it.orderId == order.orderId }.vendorOrder().status == "cancelled" ||
                    snackbarMessage() != null
        }

        val status = component.orders.value.first { it.orderId == order.orderId }.vendorOrder().status
        assertEquals("cancelled", status, "message: ${snackbarMessage()}")
        testOrder = null
        compose.onNode(hasText("Otkazana") and inOrder).assertExists()
    }

    @Test
    fun vidiNarudzbuOpensTheOrderDetails() {
        val (order, _) = ordersWithTestOrder()

        compose.onAllNodesWithText("Vidi narudžbu").onFirst().performScrollTo().performClick()
        waitForServer { openedInApp != null }

        assertEquals(order.orderId, assertIs<AppConfig.OrderDetails>(openedInApp).order.orderId)
    }

    @Test
    fun orderDetailsShowTheOrderedProduct() {
        // Without the list's screen: a test can show only one
        val (order, product) = ordersWithTestOrder(show = false)

        val details = OrderDetailsComponent(componentContext(), stateHolder, order)
        compose.setContent { OrderDetailsView(details) }
        waitForServer { !details.loader.value }

        compose.onNodeWithText("Narudžba br.${order.incrementId}").assertExists()
        compose.onNodeWithText("Detalji narudžbe po dobavljaču").performScrollTo()
        assertNotNull(
            details.order.value.orders.flatMap { it.products }.find { it.sku == product.sku },
            "${product.sku} is not in the order details"
        )
    }
}
