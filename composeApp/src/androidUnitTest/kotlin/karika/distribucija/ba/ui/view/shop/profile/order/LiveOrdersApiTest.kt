package karika.distribucija.ba.ui.view.shop.profile.order

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.profile.order.comments.CommentsComponent
import karika.distribucija.ba.ui.view.shop.profile.order.comments.CommentsView
import karika.distribucija.ba.ui.view.shop.profile.order.details.OrderDetailsComponent
import karika.distribucija.ba.ui.view.shop.profile.order.details.OrderDetailsView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
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

    /** Shows the order list as it is, without placing an order, and waits for the first page. */
    private fun showOrders(): List<OrdersResponse> {
        component = OrdersComponent(componentContext(), stateHolder)
        compose.setContent { OrdersView(component) }
        waitForLoaded()
        return component.orders.value
    }

    /** Picks [option] in the list's picker currently showing [current]. */
    private fun pick(current: String, option: String) {
        compose.onAllNodesWithText(current).onFirst().performClick()
        compose.onNode(hasText(option) and hasAnyAncestor(isPopup())).performClick()
        waitForLoaded()
    }

    @Test
    fun statusFilterShowsOnlyOrdersWithThatStatus() {
        assumeTrue("the test account has no orders", showOrders().isNotEmpty())

        pick("Sve", "Otkazana")

        val statuses = component.orders.value.map { order -> order.orders.map { it.status } }
        assertTrue(
            statuses.all { "cancelled" in it },
            "orders without a cancelled part under \"Otkazana\": $statuses"
        )
    }

    @Test
    fun oldestFirstSortsByDate() {
        assumeTrue("the test account has fewer than two orders", showOrders().size > 1)

        pick("Najnovije", "Najstarije")

        val dates = component.orders.value.mapNotNull { it.createdAt }
        assertEquals(dates.sorted(), dates, "not oldest first")
    }

    @Test
    fun newestFirstIsTheDefault() {
        val orders = showOrders()
        assumeTrue("the test account has fewer than two orders", orders.size > 1)

        val dates = orders.mapNotNull { it.createdAt }
        assertEquals(dates.sortedDescending(), dates, "not newest first")
    }

    @Test
    fun narucPonovoPutsTheOrderedProductsBackInTheCart() {
        val (order, product) = ordersWithTestOrder()

        compose.onAllNodesWithText("Naruči ponovo").onFirst().performScrollTo().performClick()
        waitForServer { openedInShop == MainConfig.Cart || snackbarMessage() != null }
        waitForServer { currentCart().items.any { it.sku == product.sku } }

        val item = currentCart().items.first { it.sku == product.sku }
        cartItemsToRemove += currentCart().items.mapNotNull { it.itemId }
        val ordered = order.orders.flatMap { it.products }.first { it.sku == product.sku }.qty?.toInt()
        assertEquals(ordered, item.qty, "quantity in the cart")
    }

    @Test
    fun commentsOfAnOrderShowAndTakeText() {
        // Without the list's screen: a test can show only one
        val (order, _) = ordersWithTestOrder(show = false)

        val comments = CommentsComponent(componentContext(), stateHolder, order.orders.first())
        compose.setContent { CommentsView(comments) }
        waitForLoaded()

        compose.onAllNodesWithText("Komentari").onFirst().assertExists()
        compose.onNodeWithText("Pošalji").assertExists()
        // Typed only: sending would reach the vendor
        compose.onNode(hasSetTextAction()).performTextInput("Test")
        assertEquals("Test", comments.newComment.value)
    }
}
