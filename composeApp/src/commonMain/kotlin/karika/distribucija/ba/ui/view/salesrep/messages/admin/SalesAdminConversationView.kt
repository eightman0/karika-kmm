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

/** Chat with the admin; the shell draws the header with the conversation's name. */
@Composable
fun SalesAdminConversationView(component: SalesAdminConversationComponent) {
    val messages by component.messages.collectAsState()
    val attachment by component.attachment.collectAsState()
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
                canSend = text.isNotBlank() || attachment != null,
                onSend = {
                    keyboard?.hide()
                    component.sendMessage(text)
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
                time = message.createdAt?.inSarajevo() ?: "",
                body = message.message(),
                attachments = message.attachments,
                onOpenPdf = { component.downloadChatAttachment(it) },
                onOpenImage = { component.showImagePreview(it) }
            )
        }
    }
}
