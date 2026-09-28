package karika.distribucija.ba.ui.view.shop.profile.messages

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.MessagesOverviewComponent
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.MessagesOverviewView
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.MessagesTestTags
import karika.distribucija.ba.ui.view.shop.profile.messages.vendor.VendorMessagesComponent
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Writing a new message as the customer test account against the real backend, through the
 * real MessagesOverviewView and MessagesOverviewComponent. Nothing is ever sent: a message
 * reaches the vendor or Karika's staff.
 */
class LiveConversationApiTest : LiveShopTest() {

    private lateinit var component: MessagesOverviewComponent

    private fun show(conversation: Conversation) {
        component = MessagesOverviewComponent(componentContext(), stateHolder, conversation)
        compose.setContent { MessagesOverviewView(component) }
    }

    /** A new conversation with a vendor still to be chosen, as "Pošalji novu poruku" opens it. */
    private fun showNewVendorMessage() {
        show(Conversation())
        waitForServer { component.vendors.value.isNotEmpty() || snackbarMessage() != null }
        assertTrue(component.vendors.value.isNotEmpty(), "no vendors to write to, message: ${snackbarMessage()}")
    }

    @Test
    fun aVendorCanBeChosenByName() {
        showNewVendorMessage()
        val vendor = component.vendors.value.first { it.name().length > 2 }

        compose.onNodeWithTag(MessagesTestTags.VENDOR_SEARCH).performClick()
        compose.onNodeWithTag(MessagesTestTags.VENDOR_SEARCH).performTextInput(vendor.name().take(4))
        waitForLoaded()
        compose.onNodeWithText(vendor.name()).performClick()

        assertEquals(vendor.entityId.toString(), component.conversationState.value.vendorId)
        assertEquals(vendor.name(), component.conversationState.value.receiverName)
        compose.onNodeWithTag(MessagesTestTags.VENDOR_SEARCH).assertDoesNotExist()
    }

    @Test
    fun subjectAndMessageTakeText() {
        showNewVendorMessage()

        compose.onNodeWithTag(MessagesTestTags.SUBJECT).performTextInput("Upit za cijenu")
        compose.onNodeWithTag(MessagesTestTags.MESSAGE).performTextInput("Poruka iz testa")

        assertEquals("Upit za cijenu", component.subject.value)
        assertEquals("Poruka iz testa", component.newMessage.value)
        compose.onNodeWithText("Pošalji").assertExists()
    }

    @Test
    fun aPhotoCanBeAttached() {
        handler.photo = "slika.jpg" to byteArrayOf(1, 2, 3)
        showNewVendorMessage()

        compose.onNodeWithContentDescription("Dodaj prilog").performClick()
        compose.onNodeWithText("Izaberi").assertExists()
        compose.onNodeWithText("Sliku iz galerije").performClick()

        assertEquals("slika.jpg", component.attachment.value?.first)
    }

    @Test
    fun aMessageToKarikaHasNoVendorToChoose() {
        show(Conversation(receiverId = "0", receiverName = "Karika Distribucija", admin = true))

        compose.onNodeWithTag(MessagesTestTags.VENDOR_SEARCH).assertDoesNotExist()
        compose.onNodeWithTag(MessagesTestTags.SUBJECT).assertExists()
    }

    @Test
    fun anExistingConversationKeepsItsSubject() {
        // Loaded without its screen: a test can show only one
        val list = VendorMessagesComponent(componentContext(), stateHolder)
        list.loadNextPage(true)
        waitForLoaded()
        val conversation = list.messages.value.firstOrNull { it.subject != null }
        assumeTrue("the test account has no conversations with vendors", conversation != null)

        show(conversation!!)

        compose.onNodeWithTag(MessagesTestTags.VENDOR_SEARCH).assertDoesNotExist()
        compose.onNodeWithTag(MessagesTestTags.SUBJECT).assertIsNotEnabled()
    }
}
