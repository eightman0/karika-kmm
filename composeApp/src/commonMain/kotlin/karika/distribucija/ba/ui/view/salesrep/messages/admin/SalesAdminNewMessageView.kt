package karika.distribucija.ba.ui.view.salesrep.messages.admin

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

private fun String?.formatTime(): String {
    if (this == null) return ""
    val timePart = this.inSarajevo().split(" ").getOrNull(1) ?: return ""
    val parts = timePart.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
}

/**
 * New message to the admin: there is only one recipient, so the screen is the composer; the
 * first message starts the conversation, which continues on this screen.
 */
@Composable
fun SalesAdminNewMessageView(component: SalesAdminNewMessageComponent) {
    val messages by component.messages.collectAsState()
    val attachment by component.attachment.collectAsState()
    val conversationId by component.conversationId.collectAsState()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf("") }

    val canSend = text.isNotBlank() || attachment != null

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    SalesChatScaffold(
        listState = listState,
        isEmpty = messages.isEmpty(),
        emptyText = if (conversationId == null) "Napišite poruku administratoru ispod." else null,
        input = {
            SalesChatInput(
                text = text,
                onTextChange = { text = it },
                canSend = canSend,
                onSend = {
                    keyboard?.hide()
                    component.send(text)
                    text = ""
                },
                attachment = attachment,
                onRemoveAttachment = { component.attachment.value = null },
                onPreviewAttachment = { component.showImagePreview(it) },
                onPickFile = component::pickFile,
                onPickPhoto = component::pickPhoto
            )
        }
    ) {
        items(messages) { message ->
            SalesChatBubble(
                mine = message.isFromVendor(),
                senderName = "Administrator",
                time = message.createdAt.formatTime(),
                body = message.message(),
                attachments = message.attachments,
                onOpenPdf = { component.downloadChatAttachment(it) },
                onOpenImage = { component.showImagePreview(it) }
            )
        }
    }
}
