package karika.distribucija.ba.ui.view.salesrep.messages.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import karika.distribucija.ba.domain.model.StaffThread
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_messages
import org.jetbrains.compose.resources.vectorResource

private fun String?.formatDate(): String {
    if (this == null) return ""
    val datePart = (this.split("T").firstOrNull() ?: this.split(" ").firstOrNull() ?: this)
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}." else this
}

@Composable
fun SalesInternalMessagesView(component: SalesInternalMessagesComponent) {
    val threads by component.threads.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(KarikaColors.Gray20)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (threads.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(KarikaColors.Gray10),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_messages),
                                contentDescription = null,
                                tint = KarikaColors.Gray6,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        KarikaText(
                            text = "Nema internih poruka",
                            color = KarikaColors.Gray2,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W600
                        )
                        KarikaText(
                            text = "Ovdje će se prikazati vaše interne\nporuke s kolegama.",
                            color = KarikaColors.Gray6,
                            textSize = 13.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            } else {
                items(threads, key = { it.threadId }) { thread ->
                    InternalThreadCard(
                        thread = thread,
                        onClick = { component.openConversation(thread) }
                    )
                }
            }
        }

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
                    text = "Nova interna poruka",
                    color = KarikaColors.White,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
private fun InternalThreadCard(thread: StaffThread, onClick: () -> Unit) {
    val isUnread = thread.hasUnread()

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

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = thread.counterpartName,
                    color = if (isUnread) KarikaColors.Gray2 else KarikaColors.Gray6,
                    textSize = if (isUnread) 15.sp else 14.sp,
                    fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W500,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                KarikaText(
                    text = (thread.lastMessageAt ?: thread.updatedAt).formatDate(),
                    color = if (isUnread) KarikaColors.Blue else KarikaColors.Gray7,
                    textSize = 11.sp,
                    fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W400
                )
            }

            Spacer(Modifier.height(2.dp))

            KarikaText(
                text = thread.displayRole(),
                color = KarikaColors.Gray7,
                textSize = 11.sp,
                fontWeight = FontWeight.W400
            )

            if (!thread.lastMessage.isNullOrEmpty()) {
                Spacer(Modifier.height(3.dp))
                KarikaText(
                    text = thread.lastMessage,
                    color = KarikaColors.Gray2,
                    textSize = if (isUnread) 14.sp else 13.sp,
                    fontWeight = if (isUnread) FontWeight.W700 else FontWeight.W400,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis
                )
            }

            if (isUnread) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KarikaColors.Blue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    KarikaText(
                        text = "NOVO  ${thread.unreadCount}",
                        color = KarikaColors.White,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }

        Icon(
            imageVector = vectorResource(Res.drawable.ic_arrow_right),
            contentDescription = "",
            tint = if (isUnread) KarikaColors.Blue else KarikaColors.Gray9,
            modifier = Modifier.size(18.dp)
        )
    }
}
