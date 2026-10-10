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
import karika.distribucija.ba.util.inSarajevo

internal fun String?.formatChatTime(): String {
    if (this == null) return ""
    val timePart = this.inSarajevo().split(" ").getOrNull(1) ?: return ""
    val parts = timePart.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
}

/** Internal chat with a colleague; the shell draws the header with the colleague's name. */
@Composable
fun SalesInternalConversationView(component: SalesInternalConversationComponent) {
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
        emptyText = null,
        input = {
            SalesChatInput(
                text = text,
                onTextChange = { text = it },
                canSend = text.isNotBlank(),
                onSend = {
                    keyboard?.hide()
                    component.sendMessage(text)
                    text = ""
                }
            )
        }
    ) {
        items(messages, key = { it.messageId ?: 0L }) { message ->
            SalesChatBubble(
                mine = component.isMine(message, me),
                senderName = component.conversation.counterpartName ?: "-",
                time = message.createdAt.formatChatTime(),
                body = message.body,
                html = false
            )
        }
    }
}
