package karika.distribucija.ba.ui.view.distributer.messages.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.conversationTag
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_send
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CustomerMessagesView(component: CustomerMessagesComponent) {
    val messages by component.messages.collectAsState()

    VendorConversationList(
        conversations = messages,
        onOpen = { component.navigateToMessagesOverview(it) },
        onNewMessage = {
            component.navigateToMessagesOverview(
                ChatConversation(
                    conversationId = null,
                    axis = ChatAxis.VENDOR_CUSTOMER,
                    counterpartName = "Nova poruka"
                )
            )
        }
    )
}

/**
 * Conversation list of the supplier's message screens (kupci, admin, interne): conversation
 * cards on the page background and a pinned "Pošalji novu poruku" button. The shell's header
 * with the screen title sits above it.
 */
@Composable
fun VendorConversationList(
    conversations: List<ChatConversation>,
    onOpen: (ChatConversation) -> Unit,
    onNewMessage: () -> Unit,
) {
    val state = rememberLazyListState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = state,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = conversations) {
                VendorConversationItem(it, onOpen)
            }
        }
        KBottomPanel {
            KPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Pošalji novu poruku",
                background = VendorAccent,
                icon = vectorResource(Res.drawable.ic_k_send),
                onClick = onNewMessage
            )
        }
    }
}

@Composable
private fun VendorConversationItem(item: ChatConversation, onOpen: (ChatConversation) -> Unit) {
    val unread = item.isUnread()
    val preview = item.lastMessagePreview?.takeIf { it.isNotBlank() }
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(conversationTag(item)),
        shape = RoundedCornerShape(14.dp),
        onClick = { onOpen(item) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(
                name = item.counterpartName,
                size = 44.dp,
                shape = CircleShape,
                background = VendorAccentSoft,
                color = VendorAccent,
                textSize = 15.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = item.counterpartName ?: "-",
                        color = KarikaUiColors.Ink,
                        textSize = 14.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = if (unread) FontWeight.W700 else FontWeight.W600,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(8.dp))
                    KarikaText(
                        text = item.dateTimeLabel() ?: "",
                        color = KarikaUiColors.Subtle,
                        textSize = 11.sp,
                        fontWeight = if (unread) FontWeight.W600 else FontWeight.W400,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = preview ?: "Nema poruka",
                        color = if (preview != null) KarikaUiColors.Muted else KarikaUiColors.Subtle,
                        textSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 1
                    )
                    if (unread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VendorAccent)
                        )
                    }
                }
            }
        }
    }
}
