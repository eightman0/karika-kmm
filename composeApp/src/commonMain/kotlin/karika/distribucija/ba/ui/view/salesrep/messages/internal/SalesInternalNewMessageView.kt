package karika.distribucija.ba.ui.view.salesrep.messages.internal

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import karika.distribucija.ba.ui.view.salesrep.messages.SalesChatBubble
import karika.distribucija.ba.ui.view.salesrep.messages.SalesChatInput
import karika.distribucija.ba.ui.view.salesrep.messages.SalesChatScaffold
import karika.distribucija.ba.ui.view.salesrep.messages.SalesRecipientPicker
import karika.distribucija.ba.ui.view.salesrep.messages.salesStaffRole

/**
 * New internal message: the colleague is picked first, then the first message starts the
 * conversation, which continues on this screen.
 */
@Composable
fun SalesInternalNewMessageView(component: SalesInternalNewMessageComponent) {
    val recipientSearch by component.recipientSearch.collectAsState()
    val filteredRecipients by component.filteredRecipients.collectAsState()
    val selectedRecipient by component.selectedRecipient.collectAsState()
    val conversationId by component.conversationId.collectAsState()
    val messages by component.messages.collectAsState()
    val me by component.stateHolder.salesSpecificHandler.me.collectAsState()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    SalesChatScaffold(
        listState = listState,
        isEmpty = messages.isEmpty(),
        emptyText = if (conversationId == null && selectedRecipient != null) {
            "Napišite prvu poruku ispod."
        } else {
            null
        },
        top = {
            if (conversationId == null) {
                SalesRecipientPicker(
                    label = "Primalac",
                    placeholder = "Pretraži sagovornika...",
                    query = recipientSearch,
                    onQueryChange = { component.setRecipientSearch(it) },
                    selected = selectedRecipient,
                    recipients = filteredRecipients,
                    onSelect = { component.selectRecipient(it) },
                    onClear = { component.clearRecipient() },
                    subtitle = { salesStaffRole(it.counterpartType) }
                )
            }
        },
        input = {
            SalesChatInput(
                text = text,
                onTextChange = { text = it },
                canSend = text.isNotBlank() && selectedRecipient != null,
                onSend = {
                    keyboard?.hide()
                    component.send(text)
                    text = ""
                }
            )
        }
    ) {
        items(messages, key = { it.messageId ?: 0L }) { message ->
            SalesChatBubble(
                mine = component.isMine(message, me),
                senderName = selectedRecipient?.name,
                time = message.createdAt.formatChatTime(),
                body = message.body,
                html = false
            )
        }
    }
}
