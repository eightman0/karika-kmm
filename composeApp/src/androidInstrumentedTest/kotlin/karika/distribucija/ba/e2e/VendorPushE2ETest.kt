package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.ChatMessageSearchResults
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.domain.model.VendorNotificationSearchResults
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.components.DASHBOARD_NOTIFICATIONS_BADGE_TAG
import karika.distribucija.ba.ui.components.conversationTag
import karika.distribucija.ba.ui.view.distributer.orders.details.ORDER_COMMENT_FIELD_TAG
import karika.distribucija.ba.ui.view.distributer.orders.details.ORDER_DETAILS_BACK_TAG
import karika.distribucija.ba.ui.view.distributer.orders.vendorOrderTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end test of push notifications for the supplier on stage.karika.ba, see [VendorE2ETest].
 * The emulator is the supplier; the customer acts through the API, as from its own phone. Stage
 * then sends a real FCM push to the emulator, so the whole path is tested: backend, FCM, the
 * device and the app's refresh. Orders the tests make are rejected at the end.
 */
@OptIn(ExperimentalTestApi::class)
class VendorPushE2ETest : VendorE2ETest() {

    // Osvježavanje otvorenih ekrana

    @Test
    fun aMessageFromTheCustomerAppearsInTheOpenConversation() {
        val conversation = conversationWithTheCustomer()
        appNotifications().cancelAll()
        goTo("Poruke kupaca")
        scrollListTo(hasTestTag(conversationTag(conversation)))
        compose.onNodeWithTag(conversationTag(conversation), useUnmergedTree = true).performClick()
        compose.waitUntilAtLeastOneExists(hasSetTextAction() and hasText("Napiši komentar"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        val text = "E2E push kupca " + System.currentTimeMillis()
        customerSends(conversation.conversationId!!, text)

        // No tap: the push alone has to bring the message onto the open conversation
        try {
            compose.waitUntil(PUSH_TIMEOUT_MS) { exists(hasText(text, substring = true) and !hasSetTextAction(), unmerged = true) }
        } catch (e: ComposeTimeoutException) {
            val onStage = messages(conversation.conversationId!!).items.any { it.body == text }
            throw AssertionError(pushDiagnosis(onStage, "open conversation", "the customer"), e)
        }
    }

    @Test
    fun aCommentFromTheCustomerAppearsOnTheOpenOrder() {
        appNotifications().cancelAll()
        val order = orderOfTheVendorsProduct()
        try {
            // The new order brings its own push, which also refreshes the order: it has to be
            // over before the comment, or it would pass for the comment's
            runCatching { compose.waitUntil(PUSH_TIMEOUT_MS) { appNotifications().activeNotifications.isNotEmpty() } }
            settleOlderOrders(order.incrementId!!)
            appNotifications().cancelAll()
            openOrder(order)

            val text = "E2E push komentar kupca " + System.currentTimeMillis()
            customerComments(order, text)

            try {
                compose.waitUntil(PUSH_TIMEOUT_MS) { exists(hasText(text, substring = true) and !hasSetTextAction(), unmerged = true) }
            } catch (e: ComposeTimeoutException) {
                val onStage = comments(order.incrementId!!).any { it.message() == text }
                throw AssertionError(pushDiagnosis(onStage, "open order's comments", "the customer"), e)
            }
        } finally {
            reject(order)
        }
    }

    @Test
    fun aNewOrderAppearsInTheOpenOrdersList() {
        appNotifications().cancelAll()
        goTo("Narudžbe", "Minimalna vrijednost narudžbe")

        val order = orderOfTheVendorsProduct()
        try {
            val tag = hasTestTag(vendorOrderTag(VendorOrder(orderId = order.incrementId)))
            try {
                compose.waitUntil(PUSH_TIMEOUT_MS) { exists(tag, unmerged = true) }
            } catch (e: ComposeTimeoutException) {
                throw AssertionError(pushDiagnosis(true, "open orders list", "the customer"), e)
            }
        } finally {
            reject(order)
        }
    }

    // Balončići

    @Test
    fun aMessageFromTheCustomerRaisesTheBadgeOnPorukeKupaca() {
        val conversation = conversationWithTheCustomer()
        // Read, so that the new message is sure to change the count
        runBlocking { ChatRepository().markRead(conversation.conversationId!!).last() }
        val before = unreadMessages()
        appNotifications().cancelAll()
        logInAsVendor()
        openDrawer()
        compose.waitUntil(SERVER_TIMEOUT_MS) { drawerBadgeShows("Poruke kupaca", before) }

        customerSends(conversation.conversationId!!, "E2E push balončić " + System.currentTimeMillis())

        var after = before
        compose.waitUntil(SERVER_TIMEOUT_MS) { unreadMessages().also { after = it } != before }
        // No tap: the push alone has to bring the new count into the open drawer
        try {
            compose.waitUntil(PUSH_TIMEOUT_MS) { drawerBadgeShows("Poruke kupaca", after) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("badge $before, $after unread on stage: " +
                pushDiagnosis(true, "badge on Poruke kupaca", "the customer"), e)
        }
    }

    @Test
    fun aNewOrderRaisesTheBadgeOnTheBell() {
        // All read, so that the badge (which stops at "9+") is sure to show the change
        runBlocking { DashRepository().markAllVendorNotificationsRead().last() }
        compose.waitUntil(SERVER_TIMEOUT_MS) { unreadNotifications() == 0 }
        appNotifications().cancelAll()
        logInAsVendor()
        compose.waitUntil(SERVER_TIMEOUT_MS) { bellShows(0) }

        val order = orderOfTheVendorsProduct()
        try {
            var after = 0
            compose.waitUntil(PUSH_TIMEOUT_MS) { unreadNotifications().also { after = it } > 0 }
            try {
                compose.waitUntil(PUSH_TIMEOUT_MS) { bellShows(after) }
            } catch (e: ComposeTimeoutException) {
                throw AssertionError("bell shows ${bellText()}, $after unread on stage: " +
                    pushDiagnosis(true, "badge on the bell", "the customer"), e)
            }
        } finally {
            reject(order)
        }
    }

    // Koraci

    private fun composer() = hasSetTextAction() and hasTestTag(ORDER_COMMENT_FIELD_TAG)

    /** Opens the supplier's [order] from "Narudžbe". */
    private fun openOrder(order: OrdersResponse) {
        goTo("Narudžbe", "Minimalna vrijednost narudžbe")
        val tag = vendorOrderTag(VendorOrder(orderId = order.incrementId))
        compose.waitUntilAtLeastOneExists(hasTestTag(tag), SERVER_TIMEOUT_MS)
        compose.onNodeWithTag(tag).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag(ORDER_DETAILS_BACK_TAG), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        // The comment bar is pinned to the bottom, outside the scrolling content
        compose.waitUntilAtLeastOneExists(composer(), SCREEN_TIMEOUT_MS)
    }

    /** Whether the drawer item [label] shows [count] in its badge (no badge for 0). */
    private fun drawerBadgeShows(label: String, count: Int): Boolean {
        val node = compose.onAllNodes(drawerItem(label)).fetchSemanticsNodes().firstOrNull() ?: return false
        val texts = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        return if (count == 0) texts.none { it.toIntOrNull() != null } else "$count" in texts
    }

    /** The bell's badge as the app writes it, "9+" above nine; none for 0. */
    private fun bellShows(count: Int) = bellText() == when {
        count == 0 -> null
        count > 9 -> "9+"
        else -> "$count"
    }

    private fun bellText(): String? = compose.onAllNodes(hasTestTag(DASHBOARD_NOTIFICATIONS_BADGE_TAG), useUnmergedTree = true)
        .fetchSemanticsNodes().firstOrNull()?.config?.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text

    // Stage: the supplier through the app's session, the customer through asAccount

    /** The conversation between the customer test account and the supplier, as the supplier has it. */
    private fun conversationWithTheCustomer(): ChatConversation {
        val vendorId = profile().entityId.toLong()
        val id = asAccount(Account.CUSTOMER) {
            val started = runBlocking { ChatRepository().startConversation(ChatAxis.VENDOR_CUSTOMER, vendorId).last() }
            assertTrue("the customer could not start the conversation: $started", started is ResultState.Success)
            ((started as ResultState.Success<*>).data as ChatConversation).conversationId!!
        }
        val result = runBlocking { ChatRepository().getConversations(ChatAxis.VENDOR_CUSTOMER, pageSize = 100).last() }
        assertTrue("conversations: $result", result is ResultState.Success)
        val conversation = ((result as ResultState.Success<*>).data as ChatConversationSearchResults).items
            .firstOrNull { it.conversationId == id }
        assertTrue("the supplier does not have conversation $id", conversation != null)
        return conversation!!
    }

    private fun customerSends(conversationId: Long, text: String) = asAccount(Account.CUSTOMER) {
        val sent = runBlocking { ChatRepository().sendMessage(conversationId, text).last() }
        assertTrue("the customer could not send the message: $sent", sent is ResultState.Success)
    }

    private fun customerComments(order: OrdersResponse, text: String) = asAccount(Account.CUSTOMER) {
        val vendorOrder = order.orders.first()
        val sent = runBlocking { OrdersRepository().sendComment(vendorOrder.orderId, vendorOrder.vendorId.toString(), text).last() }
        assertTrue("the customer could not comment: $sent", sent is ResultState.Success)
    }

    private fun messages(conversationId: Long): ChatMessageSearchResults {
        val result = runBlocking { ChatRepository().getMessages(conversationId).last() }
        assertTrue("messages: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as ChatMessageSearchResults
    }

    private fun comments(orderNumber: String): List<Comment> {
        val result = runBlocking { DashRepository().getOrderComments(orderNumber).last() }
        assertTrue("comments: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<Comment>
    }

    /** The supplier's unread customer messages, which the badge on "Poruke kupaca" shows. */
    private fun unreadMessages(): Int {
        val result = runBlocking { ChatRepository().getUnreadCount().last() }
        assertTrue("unread count: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as ChatUnreadCount).vendorCustomer
    }

    /** The supplier's unread notifications, which the bell shows. */
    private fun unreadNotifications(): Int {
        val result = runBlocking { DashRepository().vendorNotifications(isRead = false, pageSize = 1).last() }
        assertTrue("unread notifications: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as VendorNotificationSearchResults).totalCount.toInt()
    }

    private fun profile(): Vendor {
        val result = runBlocking { DashRepository().getProfile().last() }
        assertTrue("profile: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as Vendor
    }

    private fun orderOnStage(number: String): VendorOrder {
        val result = runBlocking { DashRepository().getOrder(number).last() }
        assertTrue("order: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorOrder
    }

    /** Rejects the supplier's older pending orders while they keep order [number] locked. */
    private fun settleOlderOrders(number: String) {
        repeat(MAX_SETTLED) {
            if (orderOnStage(number).locked() != true) return
            val result = runBlocking {
                DashRepository().getOrders(pageSize = 30, currentPage = 1, queryParams = listOf(NEWEST_FIRST, PENDING)).last()
            }
            @Suppress("UNCHECKED_CAST")
            val older = ((result as? ResultState.Success<*>)?.data as? List<VendorOrder>).orEmpty()
                .lastOrNull { it.orderId != number } ?: return
            runBlocking { DashRepository().changeOrderStatus("reject", older.orderId!!, "E2E: starija narudžba").last() }
        }
        assertTrue("order $number stays locked", orderOnStage(number).locked() != true)
    }

    private fun reject(order: OrdersResponse) {
        runCatching {
            runBlocking { DashRepository().changeOrderStatus("reject", order.incrementId!!, "E2E: test pusha").last() }
        }
    }

    private companion object {
        const val MAX_SETTLED = 15
        const val NEWEST_FIRST =
            "&searchCriteria[sortOrders][0][field]=created_at&searchCriteria[sortOrders][0][direction]=DESC"
        const val PENDING = "&searchCriteria[filterGroups][0][filters][0][field]=real_order_status" +
            "&searchCriteria[filterGroups][0][filters][0][value]=pending" +
            "&searchCriteria[filterGroups][0][filters][0][conditionType]=eq"
    }
}
