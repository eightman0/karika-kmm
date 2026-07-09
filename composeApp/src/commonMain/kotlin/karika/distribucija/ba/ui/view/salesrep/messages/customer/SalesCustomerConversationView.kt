package karika.distribucija.ba.ui.view.salesrep.messages.customer

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_notifications
import karikav2.composeapp.generated.resources.ic_send_receipt
import org.jetbrains.compose.resources.vectorResource

// ── Time formatter ─────────────────────────────────────────────────────────────
private fun String?.formatTime(): String {
    if (this == null) return ""
    val timePart = this.split(" ").getOrNull(1) ?: return ""
    val parts = timePart.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
}


@Composable
fun SalesCustomerConversationView(component: SalesCustomerConversationComponent) {
    val messages by component.messages.collectAsState()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        // ── Top bar ────────────────────────────────────────────────────────────
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
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = component.conversation.customerName(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis
                )
                if (!component.conversation.subject.isNullOrBlank()) {
                    KarikaText(
                        text = component.conversation.subject ?: "",
                        color = KarikaColors.Gray6,
                        textSize = 12.sp,
                        fontWeight = FontWeight.W400,
                        maxLines = 1,
                        textOverflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_notifications),
                    contentDescription = "Obavještenja",
                    tint = KarikaColors.Blue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

        // ── Messages ──────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                CustomerMessageBubble(
                    message = message,
                    customerName = component.conversation.customerName()
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
                // Attach button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.pickFile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_attachment),
                        contentDescription = "Priloži",
                        tint = KarikaColors.Gray6,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
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

                // Send button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (text.isNotBlank()) KarikaColors.Blue else KarikaColors.Gray9)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = text.isNotBlank()
                        ) {
                            keyboard?.hide()
                            component.sendMessage(text)
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

// ── Message bubble ─────────────────────────────────────────────────────────────

@Composable
private fun CustomerMessageBubble(message: Message, customerName: String) {
    // vendor sent = my message (right), customer sent = their message (left)
    val isVendor = message.sender == "vendor"

    if (isVendor) {
        // My message — right aligned, Blue bg
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
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp, topEnd = 4.dp,
                            bottomStart = 20.dp, bottomEnd = 20.dp
                        )
                    )
                    .background(KarikaColors.Blue)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (!message.message.isNullOrEmpty()) {
                    HtmlTextWithStyles(
                        html = message.message(),
                        textColor = KarikaColors.White
                    )
                }
            }
            KarikaText(
                text = message.date().formatTime(),
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        }
    } else {
        // Their message — left aligned, Primary (pink) bg
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            KarikaText(
                text = customerName,
                color = KarikaColors.Primary,
                textSize = 11.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp, topEnd = 20.dp,
                            bottomStart = 20.dp, bottomEnd = 20.dp
                        )
                    )
                    .background(KarikaColors.Primary)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (!message.message.isNullOrEmpty()) {
                    HtmlTextWithStyles(
                        html = message.message(),
                        textColor = KarikaColors.White
                    )
                }
            }
            KarikaText(
                text = message.date().formatTime(),
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}
