package karika.distribucija.ba.ui.view.salesrep.messages.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_messages
import org.jetbrains.compose.resources.vectorResource

// ── Date formatter ─────────────────────────────────────────────────────────────
private fun String?.formatDate(): String {
    if (this == null) return ""
    val datePart = this.split(" ").firstOrNull() ?: this
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}." else this
}

private val filters = listOf(
    "all"      to "Sve",
    "sent"     to "Poslano",
    "received" to "Primljeno"
)

@Composable
fun SalesCustomerMessagesView(component: SalesCustomerMessagesComponent) {
    val conversations by component.conversations.collectAsState()
    val filter by component.filter.collectAsState()

    val filtered = remember(conversations, filter) {
        when (filter) {
            "sent"     -> conversations.filter { it.sender == "vendor" }
            "received" -> conversations.filter { it.sender != "vendor" }
            else       -> conversations
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(KarikaColors.Gray20)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.goBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_arrow_back),
                        contentDescription = "Nazad",
                        tint = KarikaColors.Blue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                KarikaText(
                    text = "Poruke kupaca",
                    color = KarikaColors.Blue,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Filter chips ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, label) ->
                    val isSelected = filter == key
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) KarikaColors.Blue else KarikaColors.Gray10)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { component.setFilter(key) }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        KarikaText(
                            text = label,
                            color = if (isSelected) KarikaColors.White else KarikaColors.Gray2,
                            textSize = 12.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }

            // ── Conversation list ──────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                text = "Nema poruka",
                                color = KarikaColors.Gray6,
                                textSize = 14.sp,
                                fontWeight = FontWeight.W400
                            )
                        }
                    }
                } else {
                    items(filtered, key = { it.id ?: "" }) { conversation ->
                        CustomerConversationCard(
                            conversation = conversation,
                            onClick = { component.openConversation(conversation) }
                        )
                    }
                }
            }
        }

        // ── FAB ────────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(KarikaColors.Blue)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.openNewMessage() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_add_plus),
                    contentDescription = "",
                    tint = KarikaColors.White,
                    modifier = Modifier.size(20.dp)
                )
                KarikaText(
                    text = "Pošalji novu poruku",
                    color = KarikaColors.White,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

// ── Conversation card ──────────────────────────────────────────────────────────

@Composable
private fun CustomerConversationCard(conversation: Conversation, onClick: () -> Unit) {
    val isUnread = !conversation.isRead()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KarikaColors.White)
            .then(
                if (isUnread) Modifier.border(3.dp, KarikaColors.Blue, RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, KarikaColors.Gray9, RoundedCornerShape(20.dp))
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isUnread) KarikaColors.Blue.copy(alpha = 0.12f) else KarikaColors.Gray10
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_messages),
                contentDescription = "",
                tint = if (isUnread) KarikaColors.Blue else KarikaColors.Gray6,
                modifier = Modifier.size(22.dp)
            )
        }

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = conversation.customerName(),
                    color = if (isUnread) KarikaColors.Gray2 else KarikaColors.Gray6,
                    textSize = if (isUnread) 15.sp else 14.sp,
                    fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W500,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                KarikaText(
                    text = conversation.date().formatDate(),
                    color = if (isUnread) KarikaColors.Blue else KarikaColors.Gray7,
                    textSize = 11.sp,
                    fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W400
                )
            }

            Spacer(Modifier.height(3.dp))

            KarikaText(
                text = conversation.subject ?: "—",
                color = KarikaColors.Gray2,
                textSize = if (isUnread) 14.sp else 13.sp,
                fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W400,
                maxLines = 1,
                textOverflow = TextOverflow.Ellipsis
            )

            if (isUnread) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KarikaColors.Blue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    KarikaText(
                        text = "NOVO",
                        color = KarikaColors.White,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }

        // Chevron
        Icon(
            imageVector = vectorResource(Res.drawable.ic_arrow_right),
            contentDescription = "",
            tint = if (isUnread) KarikaColors.Blue else KarikaColors.Gray9,
            modifier = Modifier.size(18.dp)
        )
    }
}
