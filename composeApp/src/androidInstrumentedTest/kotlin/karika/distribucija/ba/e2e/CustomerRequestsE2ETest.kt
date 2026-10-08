package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.api.PartnershipRepository
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorNotification
import karika.distribucija.ba.domain.model.VendorNotificationSearchResults
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * End-to-end test of what reaches the customer on stage.karika.ba, see [CustomerE2ETest]:
 * notifications (opening one marks it read, "Označi sve kao pročitano") and partnership requests
 * from vendors ("Prihvati", "Odbij" with a reason). Both depend on what stage has for the test
 * account; without unread notifications or open requests those tests are skipped. The tests run
 * by name, so that opening one notification comes before marking them all read.
 */
@OptIn(ExperimentalTestApi::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CustomerRequestsE2ETest : CustomerE2ETest() {

    // Notifikacije

    @Test
    fun openingANotificationMarksItRead() {
        val unread = notifications(isRead = false).firstOrNull()
        assumeTrue("the customer has no unread notification", unread != null)
        openProfileButton("Notifikacije")
        compose.waitUntilAtLeastOneExists(hasText(unread!!.title), SERVER_TIMEOUT_MS)

        compose.onAllNodesWithText(unread.title).onFirst().performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) {
            notifications().firstOrNull { it.notificationId == unread.notificationId }?.isRead == true
        }
    }

    @Test
    fun oznaciSveKaoProcitanoMarksEveryNotificationRead() {
        assumeTrue("the customer has no unread notification", notifications(isRead = false).isNotEmpty())
        openProfileButton("Notifikacije")
        compose.waitUntilAtLeastOneExists(hasText("Označi sve kao pročitano"), SERVER_TIMEOUT_MS)

        compose.onAllNodesWithText("Označi sve kao pročitano").onFirst().performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) { notifications(isRead = false).isEmpty() }
        compose.waitUntilDoesNotExist(hasText("Označi sve kao pročitano"), SERVER_TIMEOUT_MS)
    }

    // Zahtjevi za partnerstvo

    @Test
    fun prihvatiAcceptsAPartnershipRequest() {
        val request = requests().firstOrNull()
        assumeTrue("no open partnership request from a vendor", request != null)
        openProfileButton("Zahtjevi za partnerstvo")
        compose.waitUntilAtLeastOneExists(hasText("Prihvati") and hasClickAction(), SERVER_TIMEOUT_MS)

        compose.onAllNodes(hasText("Prihvati") and hasClickAction()).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("prihvatiti zahtjev za partnerstvo", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Prihvati")).performClick()

        waitForSnackbar("Zahtjev za partnerstvo je prihvaćen.")
        compose.waitUntil(SERVER_TIMEOUT_MS) { requests().none { it.partnershipId == request!!.partnershipId } }
    }

    @Test
    fun odbijWithAReasonRejectsAPartnershipRequest() {
        val request = requests().firstOrNull()
        assumeTrue("no open partnership request from a vendor", request != null)
        openProfileButton("Zahtjevi za partnerstvo")
        compose.waitUntilAtLeastOneExists(hasText("Odbij") and hasClickAction(), SERVER_TIMEOUT_MS)

        compose.onAllNodes(hasText("Odbij") and hasClickAction()).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("odbiti zahtjev za partnerstvo", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNode(hasSetTextAction() and hasText("Unesite razlog odbijanja")).performTextInput("E2E razlog " + uniqueLetters())
        closeKeyboard()
        compose.onNode(dialogButton("Odbij")).performClick()

        waitForSnackbar("Zahtjev za partnerstvo je odbijen.")
        compose.waitUntil(SERVER_TIMEOUT_MS) { requests().none { it.partnershipId == request!!.partnershipId } }
    }

    @Test
    fun odustaniInThePartnershipDialogKeepsTheRequest() {
        val request = requests().firstOrNull()
        assumeTrue("no open partnership request from a vendor", request != null)
        openProfileButton("Zahtjevi za partnerstvo")
        compose.waitUntilAtLeastOneExists(hasText("Prihvati") and hasClickAction(), SERVER_TIMEOUT_MS)

        compose.onAllNodes(hasText("Prihvati") and hasClickAction()).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("prihvatiti zahtjev za partnerstvo", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNode(dialogButton("Odustani")).performClick()

        compose.waitUntilDoesNotExist(hasText("prihvatiti zahtjev za partnerstvo", substring = true), SCREEN_TIMEOUT_MS)
        assertTrue("Odustani changed the request", requests().any { it.partnershipId == request!!.partnershipId })
    }

    private fun openProfileButton(label: String) {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText(label, substring = true) and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText(label, substring = true) and hasClickAction()).performClick()
        compose.waitUntilDoesNotExist(hasText("Moj nalog") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun notifications(isRead: Boolean? = null): List<VendorNotification> {
        val result = runBlocking { NotificationRepository().vendorNotifications(isRead = isRead).last() }
        assertTrue("notifications: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as VendorNotificationSearchResults).items
    }

    private fun requests(): List<PartnershipRequest> {
        val result = runBlocking { PartnershipRepository().list().last() }
        assertTrue("partnership requests: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<PartnershipRequest>
    }
}
