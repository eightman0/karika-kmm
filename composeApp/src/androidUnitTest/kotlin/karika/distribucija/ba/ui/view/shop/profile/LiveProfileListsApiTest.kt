package karika.distribucija.ba.ui.view.shop.profile

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.profile.messages.admin.AdminMessagesComponent
import karika.distribucija.ba.ui.view.shop.profile.messages.admin.AdminMessagesView
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.MessagesOverviewComponent
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.MessagesOverviewView
import karika.distribucija.ba.ui.view.shop.profile.messages.vendor.VendorMessagesComponent
import karika.distribucija.ba.ui.view.shop.profile.messages.vendor.VendorMessagesView
import karika.distribucija.ba.ui.view.shop.profile.notifications.NotificationsComponent
import karika.distribucija.ba.ui.view.shop.profile.notifications.NotificationsView
import karika.distribucija.ba.ui.view.shop.profile.partnership.PartnershipRequestsComponent
import karika.distribucija.ba.ui.view.shop.profile.partnership.PartnershipRequestsView
import karika.distribucija.ba.ui.view.shop.profile.points.PointsComponent
import karika.distribucija.ba.ui.view.shop.profile.points.PointsView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The read-only lists behind the profile tab (messages, points, notifications, partnership
 * requests) for the customer test account against the real backend. Nothing is sent,
 * accepted or marked read: those reach other people.
 */
class LiveProfileListsApiTest : LiveShopTest() {

    @Test
    fun adminMessagesLoad() {
        val component = AdminMessagesComponent(componentContext(), stateHolder)
        compose.setContent { AdminMessagesView(component) }
        waitForLoaded()

        compose.onNodeWithText("Poruke admina").assertExists()
        assertNull(snackbarMessage(), "loading failed")
    }

    @Test
    fun newAdminMessageOpensAConversationWithKarika() {
        val component = AdminMessagesComponent(componentContext(), stateHolder)
        compose.setContent { AdminMessagesView(component) }

        compose.onNodeWithText("Pošalji novu poruku").performClick()
        waitForServer { openedInApp != null }

        val conversation = assertIs<AppConfig.MessagesOverview>(openedInApp).conversation
        assertEquals("Karika Distribucija", conversation.receiverName)
        assertEquals(true, conversation.admin)
    }

    @Test
    fun vendorMessagesLoad() {
        val component = VendorMessagesComponent(componentContext(), stateHolder)
        compose.setContent { VendorMessagesView(component) }
        waitForLoaded()

        compose.onNodeWithText("Poruke dobavljača").assertExists()
        assertNull(snackbarMessage(), "loading failed")
    }

    @Test
    fun anExistingConversationShowsItsMessages() {
        // Loaded without its screen: a test can show only one
        val list = VendorMessagesComponent(componentContext(), stateHolder)
        list.loadNextPage(true)
        waitForLoaded()
        val conversation = list.messages.value.firstOrNull()
        assumeTrue("the test account has no conversations with vendors", conversation != null)

        val component = MessagesOverviewComponent(componentContext(), stateHolder, conversation!!)
        compose.setContent { MessagesOverviewView(component) }
        waitForServer { component.messages.value.isNotEmpty() || snackbarMessage() != null }

        assertTrue(component.messages.value.isNotEmpty(), "no messages, message: ${snackbarMessage()}")
    }

    @Test
    fun pointsLoad() {
        val component = PointsComponent(componentContext(), stateHolder)
        compose.setContent { PointsView(component) }
        waitForLoaded()

        compose.onNodeWithText("Moji bodovi").assertExists()
        compose.onNodeWithText("Iznos ostvarenih bodova").assertExists()
        compose.onNodeWithText("Iznos bodova na čekanju").assertExists()
        assertNull(snackbarMessage(), "loading failed")
    }

    @Test
    fun notificationsLoad() {
        val component = NotificationsComponent(componentContext(), stateHolder)
        compose.setContent { NotificationsView(component) }
        waitForLoaded()

        compose.onNodeWithText("Notifikacije").assertExists()
        if (component.notifications.value.isEmpty()) {
            compose.onNodeWithText("Nema obavijesti").assertExists()
        } else {
            compose.onNodeWithText("Nema obavijesti").assertDoesNotExist()
        }
    }

    @Test
    fun partnershipRequestsLoad() {
        val component = PartnershipRequestsComponent(componentContext(), stateHolder)
        compose.setContent { PartnershipRequestsView(component) }
        waitForLoaded()

        compose.onNodeWithText("Zahtjevi za partnerstvo").assertExists()
        assertNull(component.error.value, "loading failed")
        if (component.requests.value.isEmpty()) {
            compose.onNodeWithText("Trenutno nemate zahtjeva za partnerstvo.").assertExists()
        }
    }

    @Test
    fun pointsLoadTheNextPageOfTransactionsAtTheEnd() {
        val component = PointsComponent(componentContext(), stateHolder)
        compose.setContent { PointsView(component) }
        waitForLoaded()
        val firstPage = component.transactions.value.size
        assumeTrue("the test account has at most one page of transactions", firstPage >= component.pageSize)

        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(firstPage)
        waitForLoaded()

        assertTrue(component.transactions.value.size > firstPage, "no second page of transactions")
    }
}
