package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.ChatMessageSearchResults
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of the customer's messages on stage.karika.ba, see [CustomerE2ETest]: a new
 * message to the admin, a new message to a vendor picked in the recipient search, a reply in an
 * existing conversation, and "Pošalji poruku dobavljaču" on a product. Every message is really
 * sent and checked on stage.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerMessagingE2ETest : CustomerE2ETest() {

    @Test
    fun aNewMessageToTheAdminIsSent() {
        openProfileButton("Poruke admina")
        compose.onNode(hasText("Pošalji novu poruku") and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        val text = send()

        val conversation = conversationWith(ChatAxis.CUSTOMER_ADMIN, text)
        assertSentOnStage(conversation, text)
    }

    @Test
    fun aNewMessageToAVendorPickedInTheSearchIsSent() {
        val vendor = recipients().firstOrNull { !it.name.isNullOrBlank() }
        assumeTrue("the customer has no vendor to write to", vendor != null)
        openProfileButton("Poruke dobavljača")
        compose.onNode(hasText("Pošalji novu poruku") and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Nova poruka"), SCREEN_TIMEOUT_MS)

        compose.onNode(hasSetTextAction() and hasText("Pretražite dobavljača")).performTextInput(vendor!!.name!!.take(6))
        compose.waitUntilAtLeastOneExists(hasText(vendor.name!!) and hasClickAction(), SERVER_TIMEOUT_MS)
        closeKeyboard()
        compose.onAllNodes(hasText(vendor.name!!) and hasClickAction()).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        val text = send()

        val conversation = conversationWith(ChatAxis.VENDOR_CUSTOMER, text)
        assertEquals(vendor.counterpartId, conversation.counterpartId)
        assertSentOnStage(conversation, text)
    }

    @Test
    fun theRecipientSearchSaysWhenNoVendorMatches() {
        openProfileButton("Poruke dobavljača")
        compose.onNode(hasText("Pošalji novu poruku") and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Nova poruka"), SCREEN_TIMEOUT_MS)

        compose.onNode(hasSetTextAction() and hasText("Pretražite dobavljača")).performTextInput("zzqxvnema")

        compose.waitUntilAtLeastOneExists(hasText("Nema rezultata za unijeti pojam 'zzqxvnema'"), SERVER_TIMEOUT_MS)
    }

    @Test
    fun aReplyInAnExistingVendorConversationIsSent() {
        val existing = conversations(ChatAxis.VENDOR_CUSTOMER).firstOrNull { !it.counterpartName.isNullOrBlank() }
        assumeTrue("the customer has no vendor conversation yet", existing != null)
        openProfileButton("Poruke dobavljača")
        compose.waitUntilAtLeastOneExists(hasText(existing!!.counterpartName!!), SERVER_TIMEOUT_MS)

        compose.onAllNodesWithText(existing.counterpartName!!).onFirst().performClick()
        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        val text = send()

        assertSentOnStage(existing, text)
    }

    @Test
    fun posaljiPorukuDobavljacuOnAProductOpensTheVendorsConversation() {
        val product = recommendedProducts().first()
        compose.waitUntilAtLeastOneExists(hasTestTag(productCardTag(product)), SERVER_TIMEOUT_MS)
        compose.onNodeWithTag(productCardTag(product)).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Min. količina", substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()

        compose.onAllNodes(hasText("Pošalji poruku dobavljaču") and hasClickAction()).onFirst().performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(composer(), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue("the conversation is not with ${product.vendorName()}", exists(hasText(product.vendorName()), unmerged = true))
        val text = send()
        val conversation = conversationWith(ChatAxis.VENDOR_CUSTOMER, text)
        assertEquals(product.vendorId(), conversation.counterpartId.toString())
    }

    private fun openProfileButton(label: String) {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText(label, substring = true) and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNode(hasText(label, substring = true) and hasClickAction()).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji novu poruku"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun composer() = hasSetTextAction() and hasText("Napiši komentar")

    /** Writes a message that differs from run to run, sends it and waits for its bubble. */
    private fun send(): String {
        val text = "E2E poruka " + uniqueLetters()
        compose.onNode(hasText("Pošalji") and hasClickAction()).assertIsNotEnabled()
        compose.onNode(composer()).performTextInput(text)
        closeKeyboard()
        compose.onNode(hasText("Pošalji") and hasClickAction()).assertIsEnabled().performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
        return text
    }

    private fun assertSentOnStage(conversation: ChatConversation, text: String) {
        // Right after a send stage can still return the messages without it
        var last = messages(conversation.conversationId!!).items.last()
        runCatching {
            compose.waitUntil(SERVER_TIMEOUT_MS) { messages(conversation.conversationId).items.last().also { last = it }.body == text }
        }
        assertEquals(text, last.body)
        assertEquals("customer", last.senderType)
    }

    private fun conversationWith(axis: ChatAxis, lastMessage: String): ChatConversation {
        lateinit var found: ChatConversation
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            conversations(axis).firstOrNull { it.lastMessagePreview?.contains(lastMessage) == true }
                ?.also { found = it } != null
        }
        return found
    }

    private fun conversations(axis: ChatAxis): List<ChatConversation> {
        val result = runBlocking { ChatRepository().getConversations(axis, pageSize = 100).last() }
        assertTrue("conversations: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as ChatConversationSearchResults).items
    }

    private fun messages(conversationId: Long): ChatMessageSearchResults {
        val result = runBlocking { ChatRepository().getMessages(conversationId).last() }
        assertTrue("messages: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as ChatMessageSearchResults
    }

    private fun recipients(): List<ChatRecipient> {
        val result = runBlocking { ChatRepository().getRecipients(ChatAxis.VENDOR_CUSTOMER).last() }
        assertTrue("recipients: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<ChatRecipient>
    }
}
