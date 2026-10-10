package karika.distribucija.ba.ui.view.salesrep.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.chatAttachment
import karika.distribucija.ba.domain.model.ChatAttachment
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.conversationTag
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.shop.profile.messages.overview.AttachmentModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_search
import karikav2.composeapp.generated.resources.ic_k_send
import karikav2.composeapp.generated.resources.ic_pdf
import org.jetbrains.compose.resources.vectorResource

/*
 * Shared building blocks of the sales rep's message screens, in the same style as the
 * supplier's (VendorConversationList, MessagesOverviewView): conversation cards, chat bubbles,
 * the pill message field and the recipient picker of a new message. The shell draws the header.
 */

/** The sales rep's role labels of the internal (staff) counterparts. */
fun salesStaffRole(counterpartType: String?): String = when (counterpartType) {
    "vendor_owner" -> "Vlasnik"
    "vendor_employee" -> "Zaposlenik"
    else -> counterpartType ?: ""
}

private fun String.isImageFileName() = lowercase().let {
    it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
        it.endsWith(".gif") || it.endsWith(".webp") || it.endsWith(".heic")
}

// ── Conversation list ──────────────────────────────────────────────────────────

@Composable
fun SalesConversationList(
    conversations: List<ChatConversation>,
    onOpen: (ChatConversation) -> Unit,
    onNewMessage: () -> Unit,
    newMessageText: String = "Pošalji novu poruku",
    emptyTitle: String = "Još nema poruka",
    emptyMessage: String = "Započnite razgovor dugmetom „$newMessageText” ispod.",
    subtitle: (ChatConversation) -> String? = { null },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KEmptyPlaceholder(
                    icon = vectorResource(Res.drawable.ic_k_chat),
                    title = emptyTitle,
                    message = emptyMessage,
                    iconTint = VendorAccent,
                    iconBackground = VendorAccentSoft
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.conversationId ?: 0L }) {
                    SalesConversationItem(it, subtitle(it), onOpen)
                }
            }
        }
        KBottomPanel {
            KPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = newMessageText,
                background = VendorAccent,
                icon = vectorResource(Res.drawable.ic_k_send),
                onClick = onNewMessage
            )
        }
    }
}

@Composable
private fun SalesConversationItem(
    item: ChatConversation,
    subtitle: String?,
    onOpen: (ChatConversation) -> Unit
) {
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
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(1.dp))
                    KarikaText(
                        text = subtitle,
                        color = KarikaUiColors.Subtle,
                        textSize = 11.sp,
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
                        fontWeight = if (unread) FontWeight.W600 else FontWeight.W400,
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

// ── Chat ───────────────────────────────────────────────────────────────────────

/**
 * Chat body on the page background: the messages list ([content]) above the message field.
 * [emptyText] shows instead of an empty list when given.
 */
@Composable
fun SalesChatScaffold(
    listState: LazyListState,
    isEmpty: Boolean,
    emptyText: String?,
    top: @Composable () -> Unit = {},
    input: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        top()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
            if (isEmpty && emptyText != null) {
                KarikaText(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    text = emptyText,
                    color = KarikaUiColors.Muted,
                    textSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        input()
    }
}

/** One message: the sales rep's own on the right in blue, the other side's on the left in white. */
@Composable
fun SalesChatBubble(
    mine: Boolean,
    senderName: String?,
    time: String,
    body: String?,
    attachments: List<ChatAttachment> = emptyList(),
    html: Boolean = true,
    onOpenPdf: ((String) -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (mine) 18.dp else 4.dp,
        bottomEnd = if (mine) 4.dp else 18.dp
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .padding(start = if (mine) 48.dp else 0.dp, end = if (mine) 0.dp else 48.dp)
                .clip(shape)
                .background(if (mine) VendorAccent else KarikaColors.White)
                .then(if (mine) Modifier else Modifier.border(1.dp, KarikaUiColors.Line, shape))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start
        ) {
            if (!mine && !senderName.isNullOrBlank()) {
                KarikaText(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = senderName,
                    color = VendorAccent,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W700
                )
            }
            SalesChatAttachments(attachments, mine, onOpenPdf, onOpenImage)
            if (!body.isNullOrEmpty()) {
                val textColor = if (mine) KarikaColors.White else KarikaUiColors.Ink
                if (html) {
                    HtmlTextWithStyles(html = body, textColor = textColor)
                } else {
                    KarikaText(
                        text = body,
                        color = textColor,
                        textSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }
            }
            if (time.isNotEmpty()) {
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = time,
                    color = if (mine) KarikaColors.White.copy(alpha = 0.8f) else KarikaUiColors.Subtle,
                    textSize = 11.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
}

@Composable
private fun SalesChatAttachments(
    attachments: List<ChatAttachment>,
    mine: Boolean,
    onOpenPdf: ((String) -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
) {
    val color = if (mine) KarikaColors.White else VendorAccent
    attachments.forEach { attachment ->
        val relpath = attachment.relpath ?: return@forEach
        if (attachment.isPdf()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mine) KarikaColors.White.copy(alpha = 0.18f) else VendorAccentSoft)
                    .then(if (onOpenPdf != null) Modifier.clickable { onOpenPdf(relpath) } else Modifier)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = vectorResource(Res.drawable.ic_pdf),
                    tint = color,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                KarikaText(
                    text = attachment.filename ?: "",
                    fontWeight = FontWeight.W600,
                    textSize = 12.sp,
                    color = color,
                    maxLines = 2
                )
            }
        } else {
            KarikaImage(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (onOpenImage != null) Modifier.onClick { onOpenImage(chatAttachment(relpath)) }
                        else Modifier
                    ),
                model = chatAttachment(relpath),
                contentScale = ContentScale.Inside
            )
        }
    }
}

/**
 * The message field at the bottom of a chat: optional attach button (shared attachment sheet),
 * pill text field and the blue send button. [onPickFile]/[onPickPhoto] null = no attachments.
 */
@Composable
fun SalesChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    canSend: Boolean,
    onSend: () -> Unit,
    attachment: Pair<String, ByteArray>? = null,
    onRemoveAttachment: () -> Unit = {},
    onPreviewAttachment: ((ByteArray) -> Unit)? = null,
    onPickFile: (() -> Unit)? = null,
    onPickPhoto: (() -> Unit)? = null,
    placeholder: String = "Napiši poruku...",
) {
    val showAttachmentSheet = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(KarikaColors.White)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (attachment != null) {
            SalesPendingAttachment(attachment, onRemoveAttachment, onPreviewAttachment)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onPickFile != null || onPickPhoto != null) {
                KCircleButton(
                    modifier = Modifier.semantics { contentDescription = "Priloži" },
                    icon = vectorResource(Res.drawable.ic_attachment),
                    size = 44.dp,
                    iconSize = 20.dp,
                    tint = if (attachment != null) VendorAccent else KarikaUiColors.Ink
                ) {
                    showAttachmentSheet.value = true
                }
            }
            BasicTextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = onTextChange,
                maxLines = 5,
                textStyle = TextStyle(
                    color = KarikaUiColors.Ink,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(VendorAccent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(KarikaUiColors.Field)
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            KarikaText(
                                text = placeholder,
                                color = KarikaUiColors.Subtle,
                                textSize = 15.sp,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                }
            )
            KPrimaryButton(
                modifier = Modifier.semantics { contentDescription = "Pošalji" },
                text = "Pošalji",
                height = 44.dp,
                background = VendorAccent,
                icon = vectorResource(Res.drawable.ic_k_send),
                enabled = canSend,
                onClick = onSend
            )
        }
    }
    if (onPickFile != null || onPickPhoto != null) {
        AttachmentModal(
            showAttachmentModal = showAttachmentSheet,
            onPickFile = { onPickFile?.invoke() },
            onPickPhoto = { onPickPhoto?.invoke() },
            accent = VendorAccent,
            accentSoft = VendorAccentSoft
        )
    }
}

@Composable
private fun SalesPendingAttachment(
    attachment: Pair<String, ByteArray>,
    onRemove: () -> Unit,
    onPreview: ((ByteArray) -> Unit)?,
) {
    val (name, bytes) = attachment
    val shape = RoundedCornerShape(10.dp)
    Box(modifier = Modifier.padding(bottom = 10.dp)) {
        if (name.isImageFileName()) {
            KarikaImage(
                modifier = Modifier
                    .padding(top = 6.dp, end = 6.dp)
                    .size(64.dp)
                    .clip(shape)
                    .then(if (onPreview != null) Modifier.onClick { onPreview(bytes) } else Modifier),
                model = bytes,
                contentScale = ContentScale.Crop
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(top = 6.dp, end = 6.dp)
                    .clip(shape)
                    .background(VendorAccentSoft)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = vectorResource(
                        if (name.lowercase().endsWith(".pdf")) Res.drawable.ic_pdf else Res.drawable.ic_attachment
                    ),
                    tint = VendorAccent,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                KarikaText(
                    text = name.take(32),
                    color = VendorAccent,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(KarikaUiColors.Ink)
                .clickable(onClick = onRemove)
                .semantics { contentDescription = "Ukloni prilog" },
            contentAlignment = Alignment.Center
        ) {
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_close),
                tint = KarikaColors.White,
                size = 12.dp
            )
        }
    }
}

// ── Recipient picker ───────────────────────────────────────────────────────────

/**
 * Recipient of a new message: a search field and, until one is picked, the matching
 * recipients in a card under it. [locked] shows the given recipient read-only.
 */
@Composable
fun SalesRecipientPicker(
    label: String,
    placeholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    selected: ChatRecipient?,
    recipients: List<ChatRecipient>,
    onSelect: (ChatRecipient) -> Unit,
    onClear: () -> Unit,
    locked: Boolean = false,
    subtitle: (ChatRecipient) -> String? = { null },
) {
    val focused = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        KFieldLabel(text = label, required = !locked, requiredColor = VendorAccent)
        if (locked) {
            KCard(modifier = Modifier.fillMaxWidth(), shape = shape) {
                RecipientRow(name = selected?.name, subtitle = selected?.let(subtitle), onClick = null)
            }
            return@Column
        }
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused.value = it.isFocused },
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            readOnly = selected != null,
            textStyle = TextStyle(
                color = if (selected != null) VendorAccent else KarikaUiColors.Ink,
                fontSize = 15.sp,
                fontWeight = if (selected != null) FontWeight.W600 else FontWeight.W400,
                fontFamily = karikaFonts()
            ),
            cursorBrush = SolidColor(VendorAccent),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(shape)
                        .background(KarikaColors.White)
                        .border(
                            1.dp,
                            if (selected == null && (focused.value || query.isNotEmpty())) VendorAccent
                            else KarikaUiColors.Border,
                            shape
                        )
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_search),
                        tint = KarikaUiColors.Muted,
                        size = 18.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            KarikaText(
                                text = placeholder,
                                color = KarikaUiColors.Subtle,
                                textSize = 15.sp,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                    if (selected != null || query.isNotEmpty()) {
                        KIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onClear()
                                    focused.value = true
                                }
                                .semantics { contentDescription = "Ukloni" },
                            icon = vectorResource(Res.drawable.ic_k_close),
                            tint = KarikaUiColors.Muted,
                            size = 18.dp
                        )
                    }
                }
            }
        )
        if (selected == null) {
            Spacer(Modifier.height(10.dp))
            if (recipients.isEmpty()) {
                if (query.isNotBlank()) {
                    KarikaText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "Nema rezultata za unijeti pojam '$query'",
                        color = KarikaUiColors.Muted,
                        textSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W500
                    )
                }
            } else {
                KCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        recipients.forEachIndexed { index, recipient ->
                            if (index > 0) {
                                KDivider()
                            }
                            RecipientRow(
                                name = recipient.name,
                                subtitle = subtitle(recipient),
                                onClick = {
                                    focused.value = false
                                    onSelect(recipient)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipientRow(name: String?, subtitle: String?, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KInitials(
            name = name,
            size = 34.dp,
            shape = CircleShape,
            background = VendorAccentSoft,
            color = VendorAccent,
            textSize = 11.sp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = name ?: "-",
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            if (!subtitle.isNullOrBlank()) {
                KarikaText(
                    text = subtitle,
                    color = KarikaUiColors.Subtle,
                    textSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}
