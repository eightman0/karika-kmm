package karika.distribucija.ba.ui.view.salesrep.messages.internal

import karika.distribucija.ba.util.inSarajevo
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_cancel_circle
import karikav2.composeapp.generated.resources.ic_send_receipt
import org.jetbrains.compose.resources.vectorResource

private fun String?.formatTime(): String {
    if (this == null) return ""
    val timePart = this.inSarajevo().split(" ").getOrNull(1) ?: return ""
    val parts = timePart.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
}

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
    var recipientFieldFocused by remember { mutableStateOf(false) }

    val showDropdown = recipientFieldFocused && selectedRecipient == null && filteredRecipients.isNotEmpty()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        // ── Compose header (shown only before first send) ──────────────────────
        if (conversationId == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recipient search/select field
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                if (showDropdown)
                                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                                else
                                    RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (recipientFieldFocused) KarikaColors.Blue else KarikaColors.Gray9,
                                if (showDropdown)
                                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                                else
                                    RoundedCornerShape(14.dp)
                            )
                            .background(KarikaColors.Gray20)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            text = "Prima:",
                            color = KarikaColors.Gray6,
                            textSize = 13.sp,
                            fontWeight = FontWeight.W600,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (recipientSearch.isEmpty()) {
                                KarikaText(
                                    text = "Pretraži sagovornika...",
                                    color = KarikaColors.Gray7,
                                    textSize = 13.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                            BasicTextField(
                                value = recipientSearch,
                                onValueChange = { component.setRecipientSearch(it) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = if (selectedRecipient != null) KarikaColors.Blue else KarikaColors.Gray2,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedRecipient != null) FontWeight.W600 else FontWeight.W500
                                ),
                                cursorBrush = SolidColor(KarikaColors.Blue),
                                singleLine = true,
                                readOnly = selectedRecipient != null,
                                onTextLayout = {},
                                decorationBox = { innerTextField ->
                                    LaunchedEffect(Unit) { recipientFieldFocused = true }
                                    innerTextField()
                                }
                            )
                        }
                        if (selectedRecipient != null || recipientSearch.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_cancel_circle),
                                contentDescription = "Ukloni",
                                tint = KarikaColors.Gray6,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        component.clearRecipient()
                                        recipientFieldFocused = true
                                    }
                            )
                        }
                    }

                    // Dropdown results
                    if (showDropdown) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                                .border(
                                    1.dp,
                                    KarikaColors.Blue,
                                    RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                                )
                                .background(KarikaColors.White)
                        ) {
                            filteredRecipients.take(6).forEachIndexed { index, recipient ->
                                RecipientDropdownRow(
                                    recipient = recipient,
                                    onClick = {
                                        component.selectRecipient(recipient)
                                        recipientFieldFocused = false
                                    }
                                )
                                if (index < minOf(filteredRecipients.size, 6) - 1) {
                                    HorizontalDivider(
                                        color = KarikaColors.Gray10,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = KarikaColors.Gray9)
        }

        // ── Messages ──────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.messageId ?: 0L }) { message ->
                InternalNewMessageBubble(
                    message = message,
                    isMine = component.isMine(message, me),
                    counterpartName = selectedRecipient?.name ?: ""
                )
            }
        }

        // ── Input area ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.White)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KarikaColors.Gray20)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        KarikaText(
                            text = "Napiši poruku...",
                            color = KarikaColors.Gray7,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W400
                        ),
                        cursorBrush = SolidColor(KarikaColors.Blue),
                        maxLines = 5
                    )
                }

                val canSend = text.isNotBlank() && selectedRecipient != null
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canSend) KarikaColors.Blue else KarikaColors.Gray9)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = canSend
                        ) {
                            keyboard?.hide()
                            component.send(text)
                            text = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_send_receipt),
                        contentDescription = "Pošalji",
                        tint = KarikaColors.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipientDropdownRow(recipient: ChatRecipient, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            text = recipient.name ?: "-",
            color = KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W500
        )
    }
}

@Composable
private fun InternalNewMessageBubble(message: ChatMessage, isMine: Boolean, counterpartName: String) {
    if (isMine) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            KarikaText(
                text = "Ja",
                color = KarikaColors.Blue,
                textSize = 11.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(KarikaColors.Blue)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                KarikaText(
                    text = message.body ?: "",
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            KarikaText(
                text = message.createdAt.formatTime(),
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            KarikaText(
                text = counterpartName,
                color = KarikaColors.Primary,
                textSize = 11.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(KarikaColors.Primary)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                KarikaText(
                    text = message.body ?: "",
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            KarikaText(
                text = message.createdAt.formatTime(),
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}
