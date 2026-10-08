package karika.distribucija.ba.e2e

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.ChatMessageSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.util.inSarajevo
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import org.junit.Assume.assumeTrue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.VendorNotificationSearchResults
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of push notifications for the customer on stage.karika.ba, see
 * [CustomerE2ETest]. The emulator is the customer; the supplier acts through the API, as from
 * its own phone. Stage then sends a real FCM push to the emulator, so the whole path is tested:
 * backend, FCM, the device and the app's refresh.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerPushE2ETest : CustomerE2ETest() {

    private lateinit var settingsBefore: Map<String, String>

    /** Stage sends no push to a customer who switched push notifications off. */
    @Before
    fun switchPushOn() {
        settingsBefore = notificationSettings()
        if (settingsBefore[PUSH] != "1") setNotificationSettings(mapOf(PUSH to "1"))
    }

    @After
    fun restoreNotificationSettings() {
        if (::settingsBefore.isInitialized && settingsBefore[PUSH] != "1") setNotificationSettings(mapOf(PUSH to settingsBefore[PUSH]!!))
    }

    @Test
    fun aMessageFromTheVendorAppearsInTheOpenConversation() {
        val conversation = conversationWithTheVendor()
        notifications().cancelAll()
        openProfileButton("Poruke dobavljača")
        compose.waitUntilAtLeastOneExists(hasText(conversation.counterpartName!!), SERVER_TIMEOUT_MS)
        compose.onAllNodesWithText(conversation.counterpartName!!).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        val text = "E2E push " + System.currentTimeMillis()
        vendorSends(conversation.conversationId!!, text)

        // No tap: the push alone has to bring the message onto the open conversation
        try {
            compose.waitUntil(PUSH_TIMEOUT_MS) { exists(hasText(text, substring = true) and !hasSetTextAction(), unmerged = true) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError(whyNotShown(messageOnStage(conversation, text), "open conversation"), e)
        }
        // Its time is Sarajevo's, not the backend's UTC
        val sent = asAccount(Account.CUSTOMER) {
            val messages = runBlocking { ChatRepository().getMessages(conversation.conversationId!!).last() }
            ((messages as ResultState.Success<*>).data as ChatMessageSearchResults).items.last { it.body == text }
        }
        val local = sent.createdAt!!.inSarajevo().take(16)
        assertTrue("the message's time $local (Sarajevo) is not shown; stage has ${sent.createdAt} (UTC)",
            exists(hasText(local, substring = true), unmerged = true))
    }

    @Test
    fun aCommentFromTheVendorAppearsOnTheOpenOrderComments() {
        val order = orderFromTheVendor()
        try {
            notifications().cancelAll()
            reopenApp()
            openOrderComments(order)

            val text = "E2E push komentar " + System.currentTimeMillis()
            asAccount(Account.VENDOR) {
                val sent = runBlocking { DashRepository().sendComment(order.incrementId!!, text).last() }
                assertTrue("the supplier could not comment: $sent", sent is ResultState.Success)
            }

            // No tap: the push alone has to bring the comment onto the open comments
            try {
                compose.waitUntil(PUSH_TIMEOUT_MS) { exists(hasText(text, substring = true) and !hasSetTextAction(), unmerged = true) }
            } catch (e: ComposeTimeoutException) {
                val onStage = asAccount(Account.CUSTOMER) {
                    val vendorOrder = order.orders.first()
                    val comments = runBlocking { OrdersRepository().comments(vendorOrder.orderId, vendorOrder.vendorId.toString()).last() }
                    @Suppress("UNCHECKED_CAST")
                    ((comments as? ResultState.Success<*>)?.data as? List<Comment>).orEmpty().any { it.message() == text }
                }
                throw AssertionError(whyNotShown(onStage, "order's open comments"), e)
            }
        } finally {
            asAccount(Account.VENDOR) {
                runBlocking { DashRepository().changeOrderStatus("reject", order.incrementId!!, "E2E: test push komentara").last() }
            }
        }
    }

    // Balončići na Profilu

    @Test
    fun aMessageFromTheVendorRaisesTheBadgeOnPorukeDobavljaca() {
        val conversation = conversationWithTheVendor()
        // Read, so that the new message is sure to change the count
        asAccount(Account.CUSTOMER) { runBlocking { ChatRepository().markRead(conversation.conversationId!!).last() } }
        val before = unreadMessages()
        notifications().cancelAll()
        reopenApp()
        openProfile()
        compose.waitUntil(SERVER_TIMEOUT_MS) { badgeShows("Poruke dobavljača", before) }

        vendorSends(conversation.conversationId!!, "E2E push balončić " + System.currentTimeMillis())

        lateinit var after: Number
        compose.waitUntil(SERVER_TIMEOUT_MS) { unreadMessages().also { after = it } != before }
        // No tap: the push alone has to bring the new count onto the Profile
        try {
            compose.waitUntil(PUSH_TIMEOUT_MS) { badgeShows("Poruke dobavljača", after.toInt()) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("badge $before, $after unread on stage: " + whyNotShown(true, "badge on Poruke dobavljača"), e)
        }
    }

    @Test
    fun anApprovedOrderRaisesTheBadgeOnNotifikacije() {
        val order = orderFromTheVendor()
        val before = unreadNotifications()
        notifications().cancelAll()
        reopenApp()
        openProfile()
        compose.waitUntil(SERVER_TIMEOUT_MS) { badgeShows("Notifikacije", before) }

        asAccount(Account.VENDOR) {
            val approved = runBlocking {
                DashRepository().changeOrderStatus("approve", order.incrementId!!, "E2E push odobreno", withDelivery = false).last()
            }
            assertTrue("the supplier could not approve the order: $approved", approved is ResultState.Success)
        }

        lateinit var after: Number
        compose.waitUntil(PUSH_TIMEOUT_MS) { unreadNotifications().also { after = it } != before }
        try {
            compose.waitUntil(PUSH_TIMEOUT_MS) { badgeShows("Notifikacije", after.toInt()) }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("badge $before, $after unread on stage: " + whyNotShown(true, "badge on Notifikacije"), e)
        }
    }

    private fun openProfile() {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Poruke dobavljača") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /** Whether the Profile's [button] shows [count] in its badge (no badge for 0). */
    private fun badgeShows(button: String, count: Int): Boolean {
        val node = compose.onAllNodes(hasText(button) and hasClickAction()).fetchSemanticsNodes().firstOrNull()
            ?: return false
        val texts = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        return if (count == 0) texts.none { it.toIntOrNull() != null } else "$count" in texts
    }

    /** The customer's unread supplier messages, which the badge on "Poruke dobavljača" shows. */
    private fun unreadMessages(): Int = asAccount(Account.CUSTOMER) {
        val result = runBlocking { ChatRepository().getUnreadCount().last() }
        assertTrue("unread count: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as ChatUnreadCount).vendorCustomer
    }

    /** The customer's unread notifications, which the badge on "Notifikacije" shows. */
    private fun unreadNotifications(): Int = asAccount(Account.CUSTOMER) {
        val result = runBlocking { NotificationRepository().vendorNotifications(isRead = false, pageSize = 1).last() }
        assertTrue("unread notifications: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as VendorNotificationSearchResults).totalCount.toInt()
    }

    /**
     * An order of one of the supplier test account's products, placed by the customer through the
     * API, as the customer's orders have it.
     */
    private fun orderFromTheVendor(): OrdersResponse {
        val vendorId = asAccount(Account.VENDOR) {
            val profile = runBlocking { DashRepository().getProfile().last() }
            assertTrue("supplier profile: $profile", profile is ResultState.Success)
            ((profile as ResultState.Success<*>).data as Vendor).entityId
        }
        val product = asAccount(Account.CUSTOMER) {
            val result = runBlocking { ProductRepository().searchProductsByCategory(vendorId = vendorId).last() }
            @Suppress("UNCHECKED_CAST")
            ((result as? ResultState.Success<*>)?.data as? List<Product>).orEmpty()
                .firstOrNull { it.hasOnStock() && it.sku != null && it.currentPrice() > 0 }
        }
        assumeTrue("the supplier has no product in stock", product != null)
        val id = placeCustomerOrderByApi(product!!)
        return asAccount(Account.CUSTOMER) {
            val result = runBlocking { OrdersRepository().orders(sortBy = "created_at", sortDirection = "DESC").last() }
            @Suppress("UNCHECKED_CAST")
            ((result as ResultState.Success<*>).data as List<OrdersResponse>).first { it.orderId == id }
        }
    }

    /** Opens [order] from "Moje narudžbe", where it is the newest, and then its comments. */
    private fun openOrderComments(order: OrdersResponse) {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Moje narudžbe") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText("Moje narudžbe") and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("#${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onAllNodesWithText("Vidi narudžbu").onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Narudžba br.${order.incrementId}", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onAllNodesWithText("Komentari(", substring = true).onFirst().performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /**
     * The conversation between the supplier test account and the customer test account, as the
     * customer sees it; the supplier starts it if there is none yet.
     */
    private fun conversationWithTheVendor(): ChatConversation {
        val customerId = asAccount(Account.CUSTOMER) {
            val user = runBlocking { UserRepository().get().last() }
            assertTrue("customer: $user", user is ResultState.Success)
            ((user as ResultState.Success<*>).data as UserDetails).id!!.toLong()
        }
        val id = asAccount(Account.VENDOR) {
            val started = runBlocking { ChatRepository().startConversation(ChatAxis.VENDOR_CUSTOMER, customerId).last() }
            assertTrue("the supplier could not start the conversation: $started", started is ResultState.Success)
            ((started as ResultState.Success<*>).data as ChatConversation).conversationId!!
        }
        val conversation = asAccount(Account.CUSTOMER) {
            customerConversations().firstOrNull { it.conversationId == id }
        }
        assertTrue("the customer does not have conversation $id", conversation?.counterpartName != null)
        return conversation!!
    }

    private fun vendorSends(conversationId: Long, text: String) = asAccount(Account.VENDOR) {
        val sent = runBlocking { ChatRepository().sendMessage(conversationId, text).last() }
        assertTrue("the supplier could not send the message: $sent", sent is ResultState.Success)
    }

    private fun messageOnStage(conversation: ChatConversation, text: String) = asAccount(Account.CUSTOMER) {
        val messages = runBlocking { ChatRepository().getMessages(conversation.conversationId!!).last() }
        ((messages as? ResultState.Success<*>)?.data as? ChatMessageSearchResults)?.items.orEmpty()
            .any { it.body == text }
    }

    /**
     * Says where a message the supplier sent stopped: on stage, in the push, or in the refresh of
     * the [screen]. The test clears the app's notifications first, so any shown since came with a push.
     */
    private fun whyNotShown(onStage: Boolean, screen: String): String {
        val notified = notifications().activeNotifications.isNotEmpty()
        val settings = asAccount(Account.CUSTOMER) {
            val user = runBlocking { UserRepository().get().last() }
            ((user as? ResultState.Success<*>)?.data as? UserDetails)?.customAttributes.orEmpty()
                .filter { it.attributeCode.orEmpty().startsWith("notification_") }
                .joinToString { "${it.attributeCode}=${it.value}" }
        }
        return when {
            !onStage -> "it is not on stage, so the supplier's send did not get through"
            !notified -> "it is on stage, but no push arrived within ${PUSH_TIMEOUT_MS / 1000} s " +
                "(no notification was shown); the customer's settings on stage: [$settings]"
            else -> "the push arrived (its notification is shown), but the $screen did not refresh"
        }
    }

    private fun customerConversations(): List<ChatConversation> {
        val result = runBlocking { ChatRepository().getConversations(ChatAxis.VENDOR_CUSTOMER, pageSize = 100).last() }
        assertTrue("conversations: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as ChatConversationSearchResults).items
    }

    /** The app's own notifications; the test runs in the app's process. */
    private fun notifications() = InstrumentationRegistry.getInstrumentation().targetContext
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun openProfileButton(label: String) {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText(label, substring = true) and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText(label, substring = true) and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji novu poruku"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun composer() = hasSetTextAction() and hasText("Napiši komentar")

    private companion object {
        /** On stage the push usually arrives within seconds, but its queue can lag half a minute and more. */
        const val PUSH_TIMEOUT_MS = 60_000L
        const val PUSH = "notification_push_enabled"
    }
}
