package karika.distribucija.ba.ui.view.shop.profile.messages.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.conversationTag
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_send
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AdminMessagesView(component: AdminMessagesComponent) {
    MessagesScreen(
        title = "Poruke admina",
        component = component
    ) {
        component.navigateToMessagesOverview(
            ChatConversation(
                conversationId = null,
                axis = ChatAxis.CUSTOMER_ADMIN,
                counterpartName = "Karika Distribucija"
            )
        )
    }
}

/**
 * Conversation list screen shared by "Poruke admina" and "Poruke dobavljača": white header,
 * conversation cards and a pinned "Pošalji novu poruku" button. Drawn at the app root, so it
 * paints the status bar area white and keeps the navigation bar inside the bottom panel.
 */
@Composable
fun MessagesScreen(
    title: String,
    component: AdminMessagesComponent,
    onNewMessage: () -> Unit,
) {
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(title = title, onBack = { component.appBack() })
            }
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                KBottomPanel {
                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Pošalji novu poruku",
                        icon = vectorResource(Res.drawable.ic_k_send),
                        onClick = onNewMessage
                    )
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
        component = component
    ) {
        Box(modifier = Modifier.fillMaxSize().background(KarikaUiColors.Page)) {
            MessageList(component)
        }
    }
}

@Composable
fun MessageList(component: AdminMessagesComponent) {
    val messages by component.messages.collectAsState()

    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            KEmptyPlaceholder(
                icon = vectorResource(Res.drawable.ic_k_chat),
                title = "Još nema poruka",
                message = "Započnite razgovor dugmetom „Pošalji novu poruku” ispod."
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = messages) { item ->
            MessageItem(item, component)
        }
    }
}

@Composable
private fun MessageItem(item: ChatConversation, component: AdminMessagesComponent) {
    val unread = item.isUnread()
    val preview = item.lastMessagePreview?.takeIf { it.isNotBlank() }
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(conversationTag(item)),
        shape = RoundedCornerShape(14.dp),
        onClick = { component.navigateToMessagesOverview(item) }
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
                                .background(KarikaUiColors.Pink)
                        )
                    }
                }
            }
        }
    }
}
