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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.chatImage
import karika.distribucija.ba.domain.model.FileData
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_cancel_circle
import karikav2.composeapp.generated.resources.ic_pdf
import karikav2.composeapp.generated.resources.ic_photo
import karikav2.composeapp.generated.resources.ic_send_receipt
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.vectorResource

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun String?.formatTime(): String {
    if (this == null) return ""
    val timePart = this.split(" ").getOrNull(1) ?: return ""
    val parts = timePart.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
}

private fun String.isImageFile() = lowercase().let {
    it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
    it.endsWith(".gif") || it.endsWith(".webp")
}

// ── View ───────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesCustomerNewMessageView(component: SalesCustomerNewMessageComponent) {
    val subject by component.subject.collectAsState()
    val customerSearch by component.customerSearch.collectAsState()
    val customers by component.customers.collectAsState()
    val selectedCustomer by component.selectedCustomer.collectAsState()
    val threadId by component.threadId.collectAsState()
    val messages by component.messages.collectAsState()
    val attachment by component.attachment.collectAsState()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    var customerFieldFocused by remember { mutableStateOf(false) }

    val canSend = text.isNotBlank() || attachment != null
    val showDropdown = customerFieldFocused && selectedCustomer == null && customers.isNotEmpty()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        // ── Compose header (shown only before first send) ──────────────────────
        if (threadId == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subject field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                        .background(KarikaColors.Gray20)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        text = "Predmet:",
                        color = KarikaColors.Gray6,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (subject.isEmpty()) {
                            KarikaText(
                                text = "Unesite predmet poruke...",
                                color = KarikaColors.Gray7,
                                textSize = 13.sp,
                                fontWeight = FontWeight.W400
                            )
                        }
                        BasicTextField(
                            value = subject,
                            onValueChange = { component.setSubject(it) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = KarikaColors.Gray2,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W500
                            ),
                            cursorBrush = SolidColor(KarikaColors.Blue),
                            singleLine = true
                        )
                    }
                }

                // Customer search/select field
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
                                if (customerFieldFocused) KarikaColors.Blue else KarikaColors.Gray9,
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
                            text = "Kupac:",
                            color = KarikaColors.Gray6,
                            textSize = 13.sp,
                            fontWeight = FontWeight.W600,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (customerSearch.isEmpty()) {
                                KarikaText(
                                    text = "Pretraži kupca...",
                                    color = KarikaColors.Gray7,
                                    textSize = 13.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                            BasicTextField(
                                value = customerSearch,
                                onValueChange = { component.setCustomerSearch(it) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = if (selectedCustomer != null) KarikaColors.Blue else KarikaColors.Gray2,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedCustomer != null) FontWeight.W600 else FontWeight.W500
                                ),
                                cursorBrush = SolidColor(KarikaColors.Blue),
                                singleLine = true,
                                readOnly = selectedCustomer != null,
                                onTextLayout = {},
                                decorationBox = { innerTextField ->
                                    LaunchedEffect(Unit) { customerFieldFocused = true }
                                    innerTextField()
                                }
                            )
                        }
                        if (selectedCustomer != null || customerSearch.isNotEmpty()) {
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
                                        component.clearCustomer()
                                        customerFieldFocused = true
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
                            customers.take(6).forEachIndexed { index, customer ->
                                CustomerRow(
                                    customer = customer,
                                    onClick = {
                                        component.selectCustomer(customer)
                                        customerFieldFocused = false
                                    }
                                )
                                if (index < minOf(customers.size, 6) - 1) {
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
            items(messages) { message ->
                CustomerNewMessageBubble(
                    message = message,
                    customerName = selectedCustomer?.company ?: selectedCustomer?.fullName ?: ""
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
            // Attachment thumbnail
            if (attachment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KarikaColors.Blue.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = vectorResource(
                            if (attachment!!.first.isImageFile()) Res.drawable.ic_photo
                            else Res.drawable.ic_attachment
                        ),
                        contentDescription = null,
                        tint = KarikaColors.Blue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    KarikaText(
                        text = attachment!!.first.take(32),
                        color = KarikaColors.Blue,
                        textSize = 12.sp,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_cancel_circle),
                        contentDescription = "Ukloni prilog",
                        tint = KarikaColors.Gray6,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { component.attachment.value = null }
                    )
                }
            }

            // Text input row
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showAttachSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_attachment),
                        contentDescription = "Priloži",
                        tint = if (attachment != null) KarikaColors.Blue else KarikaColors.Gray6,
                        modifier = Modifier.size(22.dp)
                    )
                }

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

    // ── Attach bottom sheet ────────────────────────────────────────────────────
    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = KarikaColors.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                KarikaText(
                    text = "Dodaj prilog",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
                HorizontalDivider(color = KarikaColors.Gray9)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showAttachSheet = false
                            component.pickFile()
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KarikaColors.Blue.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_attachment),
                            contentDescription = null,
                            tint = KarikaColors.Blue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        KarikaText(text = "Fajl", color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W600)
                        KarikaText(text = "Dokument, PDF, tabela...", color = KarikaColors.Gray6, textSize = 12.sp, fontWeight = FontWeight.W400)
                    }
                }

                HorizontalDivider(color = KarikaColors.Gray10, modifier = Modifier.padding(horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showAttachSheet = false
                            component.pickPhoto()
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KarikaColors.Blue.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_photo),
                            contentDescription = null,
                            tint = KarikaColors.Blue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        KarikaText(text = "Slika", color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W600)
                        KarikaText(text = "Fotografija iz galerije ili kamere", color = KarikaColors.Gray6, textSize = 12.sp, fontWeight = FontWeight.W400)
                    }
                }
            }
        }
    }
}

// ── Customer row ───────────────────────────────────────────────────────────────

@Composable
private fun CustomerRow(customer: OperationalCustomer, onClick: () -> Unit) {
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
        Column {
            KarikaText(
                text = customer.company ?: customer.fullName,
                color = KarikaColors.Gray2,
                textSize = 13.sp,
                fontWeight = FontWeight.W500
            )
            if (!customer.company.isNullOrEmpty()) {
                KarikaText(
                    text = customer.fullName,
                    color = KarikaColors.Gray6,
                    textSize = 11.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
}

// ── Attachment renderer ────────────────────────────────────────────────────────

@Composable
private fun CustomerNewMessageAttachment(images: String?) {
    val filename = images
        ?.takeIf { it.isNotEmpty() }
        ?.let { runCatching { Json.decodeFromString<FileData>(it) }.getOrNull() }
        ?.filename
        ?.firstOrNull()
        ?.takeIf { it.isNotEmpty() }
        ?: return

    if (filename.endsWith("pdf", ignoreCase = true)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_pdf),
                contentDescription = null,
                tint = KarikaColors.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            KarikaText(text = filename, color = KarikaColors.White, textSize = 12.sp, fontWeight = FontWeight.W500)
        }
    } else {
        KarikaImage(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp)),
            model = chatImage(filename),
            contentScale = ContentScale.Inside
        )
    }
}

// ── Message bubble ─────────────────────────────────────────────────────────────

@Composable
private fun CustomerNewMessageBubble(message: Message, customerName: String) {
    val isVendor = message.sender == "vendor"

    if (isVendor) {
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
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(KarikaColors.Blue)
            ) {
                CustomerNewMessageAttachment(images = message.images)
                if (!message.message.isNullOrEmpty()) {
                    HtmlTextWithStyles(
                        html = message.message(),
                        textColor = KarikaColors.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
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
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(KarikaColors.Primary)
            ) {
                CustomerNewMessageAttachment(images = message.images)
                if (!message.message.isNullOrEmpty()) {
                    HtmlTextWithStyles(
                        html = message.message(),
                        textColor = KarikaColors.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
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
