package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.ChatMessageSearchResults
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.components.conversationTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of the sales rep's messages on stage.karika.ba, see [SalesRepE2ETest]: a new
 * message to the customer test account (which the customer then has), a reply in a conversation
 * with it, a message to the admin and an internal message to a colleague. The messages stay on
 * stage; each one says "E2E" and differs from run to run.
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepMessagingE2ETest : SalesRepE2ETest() {

    @Test
    fun aNewMessageToTheTestCustomerReachesTheCustomer() {
        val customer = testCustomer()
        val recipient = recipients(ChatAxis.VENDOR_CUSTOMER).firstOrNull { it.counterpartId == customer.customerId }
        assumeTrue("the test customer is not among the message recipients", recipient?.name != null)
        goTo("Poruke kupaca", under = "Poruke")
        compose.onNodeWithText("Pošalji novu poruku").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Nova poruka"), SCREEN_TIMEOUT_MS)

        pickRecipient(recipient!!.name!!)
        val text = send()

        // The customer has it, from the supplier
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            asAccount(Account.CUSTOMER) {
                conversations(ChatAxis.VENDOR_CUSTOMER).any { it.lastMessagePreview?.contains(text) == true }
            }
        }
    }

    @Test
    fun aReplyInTheConversationWithTheTestCustomerIsSent() {
        val customer = testCustomer()
        val conversation = conversations(ChatAxis.VENDOR_CUSTOMER).firstOrNull { it.counterpartId == customer.customerId }
        assumeTrue("the sales rep has no conversation with the test customer", conversation != null)
        openConversation(conversation!!)

        val text = send()

        compose.waitUntil(SERVER_TIMEOUT_MS) { messages(conversation.conversationId!!).items.lastOrNull()?.body == text }
    }

    @Test
    fun aNewMessageToTheAdminIsSent() {
        goTo("Poruke admina", under = "Poruke")
        compose.onNodeWithText("Pošalji novu poruku").performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Napiši poruku..."), unmerged = true) }

        val text = send()

        compose.waitUntil(SERVER_TIMEOUT_MS) {
            conversations(ChatAxis.VENDOR_ADMIN).any { it.lastMessagePreview?.contains(text) == true }
        }
    }

    @Test
    fun anInternalMessageToAColleagueIsSent() {
        val colleague = recipients(ChatAxis.STAFF).let { all ->
            all.firstOrNull { it.counterpartType == "vendor_owner" } ?: all.firstOrNull()
        }
        assumeTrue("the sales rep has no colleague to write to", colleague?.name != null)
        goTo("Interne poruke", under = "Poruke")
        compose.onNodeWithText("Nova interna poruka").performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Pretraži sagovornika..."), unmerged = true) }

        pickRecipient(colleague!!.name!!)
        val text = send()

        lateinit var conversation: ChatConversation
        compose.waitUntil(SERVER_TIMEOUT_MS) {
            conversations(ChatAxis.STAFF).firstOrNull { it.lastMessagePreview?.contains(text) == true }
                ?.also { conversation = it } != null
        }
        assertEquals(colleague.counterpartId, conversation.counterpartId)
    }

    @Test
    fun theSendButtonDoesNothingWithoutAMessage() {
        goTo("Poruke admina", under = "Poruke")
        compose.onNodeWithText("Pošalji novu poruku").performClick()
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Napiši poruku..."), unmerged = true) }
        val before = conversations(ChatAxis.VENDOR_ADMIN).map { it.lastMessagePreview }

        compose.onNodeWithContentDescription("Pošalji").performClick()
        waitUntilLoaded()

        assertTrue(exists(hasText("Napiši poruku..."), unmerged = true))
        assertEquals(before, conversations(ChatAxis.VENDOR_ADMIN).map { it.lastMessagePreview })
    }

    /**
     * Opens [conversation] from "Poruke kupaca". In the test, the conversation's scroll to its
     * last message can run in the middle of a layout pass and throw (a real app runs it a frame
     * later), so the app is started again and the conversation opened once more when that happens.
     */
    private fun openConversation(conversation: ChatConversation) {
        repeat(OPEN_ATTEMPTS) { attempt ->
            try {
                goTo("Poruke kupaca", under = "Poruke")
                // Newest first, so it is usually on screen already
                val tag = hasTestTag(conversationTag(conversation))
                if (!exists(tag, unmerged = true)) scrollListTo(tag)
                compose.waitUntil(SERVER_TIMEOUT_MS) { exists(tag, unmerged = true) }
                compose.onNodeWithTag(conversationTag(conversation), useUnmergedTree = true).performClick()
                compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Napiši poruku..."), unmerged = true) }
                waitUntilLoaded()
                return
            } catch (e: IllegalArgumentException) {
                if (e.message?.contains("during measure layout") != true || attempt == OPEN_ATTEMPTS - 1) throw e
                logInToDashboard()
            }
        }
    }

    /** Types [name] in the recipient search and picks it from the list under it. */
    private fun pickRecipient(name: String) {
        field(0).performTextInput(name)
        val row = hasText(name) and hasClickAction() and !hasSetTextAction()
        compose.waitUntilAtLeastOneExists(row, SCREEN_TIMEOUT_MS)
        compose.onAllNodes(row).onFirst().performClick()
        closeKeyboard()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * Writes a message that differs from run to run in the conversation's message field (the
     * screen's last text field), sends it and waits for its bubble.
     */
    private fun send(): String {
        val text = "E2E poruka komercijaliste " + System.currentTimeMillis()
        lastField().performTextInput(text)
        closeKeyboard()
        compose.onNodeWithContentDescription("Pošalji").performClick()
        // The conversation may already have older messages, with the new one at the end
        scrollListTo(hasText(text, substring = true) and !hasSetTextAction())
        return text
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

    private fun recipients(axis: ChatAxis): List<ChatRecipient> {
        val result = runBlocking { ChatRepository().getRecipients(axis).last() }
        assertTrue("recipients: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<ChatRecipient>
    }

    private companion object {
        const val OPEN_ATTEMPTS = 3
    }
}
