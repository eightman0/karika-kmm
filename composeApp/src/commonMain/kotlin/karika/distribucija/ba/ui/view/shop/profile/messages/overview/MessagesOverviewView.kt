package karika.distribucija.ba.ui.view.shop.profile.messages.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.chatAttachment
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.components.KBackButton
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KHeader
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.util.inSarajevo
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_camera
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_image
import karikav2.composeapp.generated.resources.ic_k_search
import karikav2.composeapp.generated.resources.ic_k_send
import karikav2.composeapp.generated.resources.ic_pdf
import karikav2.composeapp.generated.resources.ic_photo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MessagesOverviewView(component: MessagesOverviewComponent) {
    val comments = component.messages.collectAsState()
    val state = rememberLazyListState()
    val conversation by component.conversationState.asState()

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
                ChatHeader(
                    name = conversation.counterpartName ?: "-",
                    onBack = { component.appBack() }
                )
            }
        },
        bottomBar = {
            EnterComment(component)
        },
        ignoreImeTweak = true,
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            SearchForRecipient(component)
            if (comments.value.isEmpty() && conversation.conversationId != null) {
                KEmptyState(
                    modifier = Modifier.weight(1f),
                    text = "Još nema poruka.\nNapišite prvu poruku ispod."
                )
            } else {
                LazyColumn(
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = comments.value) { item ->
                        MessageItem(item, component)
                    }
                }
            }
        }
    }

    LaunchedEffect(comments.value) {
        component.scope.launch {
            state.scrollToItem(comments.value.size)
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (comments.value.lastIndex > 0) {
            state.scrollToItem(comments.value.lastIndex)
        }
    }
}

/** White header with back, the other side's initials and name. */
@Composable
private fun ChatHeader(name: String, onBack: () -> Unit) {
    KHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            KInitials(
                name = name,
                size = 40.dp,
                shape = CircleShape,
                textSize = 13.sp
            )
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = name,
                color = KarikaUiColors.Ink,
                textSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2
            )
        }
    }
}

/** Picking the supplier of a new conversation, shown until one is chosen. */
@Composable
private fun SearchForRecipient(component: MessagesOverviewComponent) {
    val conversation = component.conversationState.asState()
    if (conversation.value.conversationId != null ||
        conversation.value.axis != ChatAxis.VENDOR_CUSTOMER ||
        conversation.value.counterpartId != null
    ) {
        return
    }
    val searchText = remember { mutableStateOf("") }
    val recipients by component.recipients.collectAsState()
    val filteredRecipients by remember(searchText.value, recipients) {
        derivedStateOf {
            if (searchText.value.isBlank()) {
                recipients
            } else {
                recipients.filter { it.name?.contains(searchText.value, ignoreCase = true) == true }
            }
        }
    }
    val expand = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        KFieldLabel(text = "Dobavljač", required = true)
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) {
                        expand.value = true
                    }
                },
            value = searchText.value,
            onValueChange = {
                searchText.value = it
                expand.value = true
            },
            singleLine = true,
            textStyle = TextStyle(
                color = KarikaUiColors.Ink,
                fontSize = 15.sp,
                fontFamily = karikaFonts()
            ),
            cursorBrush = SolidColor(KarikaUiColors.Pink),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { expand.value = true }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(shape)
                        .background(KarikaColors.White)
                        .border(1.dp, if (expand.value) KarikaUiColors.Pink else KarikaUiColors.Border, shape)
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
                        if (searchText.value.isEmpty()) {
                            KarikaText(
                                text = "Pretražite dobavljača",
                                color = KarikaUiColors.Subtle,
                                textSize = 15.sp,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                    if (searchText.value.isNotEmpty()) {
                        KIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    searchText.value = ""
                                    expand.value = true
                                },
                            icon = vectorResource(Res.drawable.ic_k_close),
                            tint = KarikaUiColors.Muted,
                            size = 18.dp
                        )
                    }
                }
            }
        )
        if (expand.value) {
            Spacer(Modifier.height(10.dp))
            if (filteredRecipients.isEmpty()) {
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = "Nema rezultata za unijeti pojam '${searchText.value}'",
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500
                )
            } else {
                KCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        filteredRecipients.forEachIndexed { index, recipient ->
                            if (index > 0) {
                                KDivider()
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expand.value = false
                                        searchText.value = recipient.name ?: ""
                                        component.selectRecipient(recipient)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KInitials(
                                    name = recipient.name,
                                    size = 34.dp,
                                    shape = CircleShape,
                                    textSize = 11.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                KarikaText(
                                    modifier = Modifier.weight(1f),
                                    text = recipient.name ?: "-",
                                    color = KarikaUiColors.Ink,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W600,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterComment(component: MessagesOverviewComponent) {
    val comment = component.newMessage.asState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val conversation = component.conversationState.asState()
    val attachment = component.attachment.asState()

    val enableButton = remember(comment, conversation, attachment) {
        derivedStateOf {
            (comment.value.isNotEmpty() || attachment.value != null) &&
                    conversation.value.conversationId != null
        }
    }
    val pickAttachment = component.showAttachmentSheet.asState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        attachment.value?.second?.let {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .size(64.dp)
            ) {
                val shape = RoundedCornerShape(10.dp)
                if (attachment.value?.first?.contains("pdf") == true) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .fillMaxSize()
                            .clip(shape)
                            .background(KarikaUiColors.Field),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = vectorResource(Res.drawable.ic_pdf),
                            tint = KarikaUiColors.Pink,
                            contentDescription = null
                        )
                    }
                } else {
                    KarikaImage(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .fillMaxSize()
                            .clip(shape)
                            .onClick {
                                component.showImagePreview(it)
                            },
                        model = it,
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(KarikaUiColors.Ink)
                        .clickable {
                            component.attachment.value = null
                        },
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KCircleButton(
                icon = vectorResource(if (isKiosk()) Res.drawable.ic_camera else Res.drawable.ic_attachment),
                size = 44.dp,
                iconSize = 20.dp
            ) {
                if (isKiosk()) {
                    component.pickFile()
                } else {
                    pickAttachment.negate()
                }
            }
            BasicTextField(
                modifier = Modifier.weight(1f),
                value = comment.value,
                onValueChange = { comment.value = it },
                maxLines = 4,
                textStyle = TextStyle(
                    color = KarikaUiColors.Ink,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(KarikaUiColors.Pink),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
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
                        if (comment.value.isEmpty()) {
                            KarikaText(
                                text = "Napiši komentar",
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
                text = "Pošalji",
                height = 44.dp,
                icon = vectorResource(Res.drawable.ic_k_send),
                enabled = enableButton.value
            ) {
                keyboardController?.hide()
                component.sendMessage()
            }
        }

        AttachmentModal(
            showAttachmentModal = pickAttachment,
            onPickFile = component::pickFile,
            onPickPhoto = component::pickPhoto
        )
    }
}

@Composable
fun MessageItem(message: ChatMessage, component: MessagesOverviewComponent) {
    val mine = message.isFromCustomer()
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .padding(start = if (mine) 48.dp else 0.dp, end = if (mine) 0.dp else 48.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (mine) 18.dp else 4.dp,
                        bottomEnd = if (mine) 4.dp else 18.dp
                    )
                )
                .background(if (mine) KarikaUiColors.Pink else KarikaColors.White)
                .then(
                    if (mine) {
                        Modifier
                    } else {
                        Modifier.border(
                            1.dp,
                            KarikaUiColors.Line,
                            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                        )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start
        ) {
            if (!mine && !message.senderDisplayName.isNullOrEmpty()) {
                KarikaText(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = message.senderDisplayName.orEmpty(),
                    color = KarikaUiColors.Pink,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W700
                )
            }
            MessageAttachments(message, component, mine)
            if (!message.body.isNullOrEmpty()) {
                HtmlTextWithStyles(
                    modifier = Modifier,
                    html = message.message(),
                    textColor = if (mine) KarikaColors.White else KarikaUiColors.Ink
                )
            }
            KarikaText(
                modifier = Modifier.padding(top = 4.dp),
                text = message.createdAt?.inSarajevo() ?: "",
                color = if (mine) KarikaColors.White.copy(alpha = 0.8f) else KarikaUiColors.Subtle,
                textSize = 11.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}

@Composable
private fun MessageAttachments(message: ChatMessage, component: MessagesOverviewComponent, mine: Boolean) {
    val color = if (mine) KarikaColors.White else KarikaUiColors.Pink
    message.attachments.forEach { attachment ->
        val relpath = attachment.relpath ?: return@forEach
        if (attachment.isPdf()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (mine) KarikaColors.White.copy(alpha = 0.18f) else KarikaUiColors.PinkSoft)
                    .clickable {
                        component.downloadChatAttachment(relpath)
                    }
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
                    .onClick {
                        component.showImagePreview(chatAttachment(relpath))
                    },
                model = chatAttachment(relpath),
                contentScale = ContentScale.Inside
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentModal(
    showAttachmentModal: MutableState<Boolean>,
    onPickFile: () -> Unit = { },
    onPickPhoto: () -> Unit = { },
    accent: Color = KarikaUiColors.Pink,
    accentSoft: Color = KarikaUiColors.PinkSoft,
) {

    if (showAttachmentModal.value) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentModal.value = false },
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(KarikaUiColors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
            ) {
                KarikaText(
                    text = "Dodaj prilog",
                    fontWeight = FontWeight.W800,
                    textSize = 22.sp,
                    lineHeight = 28.sp,
                    color = KarikaUiColors.Ink
                )
                Spacer(Modifier.height(4.dp))
                KarikaText(
                    text = "Pošaljite sliku ili dokument uz poruku.",
                    textSize = 14.sp,
                    lineHeight = 20.sp,
                    color = KarikaUiColors.Muted
                )
                Spacer(Modifier.height(18.dp))
                KCard(modifier = Modifier.fillMaxWidth()) {
                    AttachmentOption(
                        icon = vectorResource(Res.drawable.ic_k_image),
                        title = "Slika iz galerije",
                        subtitle = "Fotografija sa telefona",
                        accent = accent,
                        accentSoft = accentSoft
                    ) {
                        onPickPhoto()
                        showAttachmentModal.value = false
                    }
                    KDivider()
                    AttachmentOption(
                        icon = vectorResource(Res.drawable.ic_k_document),
                        title = "Fajl sa uređaja",
                        subtitle = "PDF ili drugi dokument",
                        accent = accent,
                        accentSoft = accentSoft
                    ) {
                        onPickFile()
                        showAttachmentModal.value = false
                    }
                }
                Spacer(Modifier.height(14.dp))
                KSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Odustani"
                ) {
                    showAttachmentModal.value = false
                }
            }
        }
    }
}

/** Row of the attachment sheet: tinted icon tile, title, hint and chevron. */
@Composable
private fun AttachmentOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    accentSoft: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentSoft),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = icon, tint = accent, size = 22.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = title,
                fontWeight = FontWeight.W700,
                textSize = 15.sp,
                lineHeight = 20.sp,
                color = KarikaUiColors.Ink,
                maxLines = 1
            )
            KarikaText(
                text = subtitle,
                textSize = 12.5.sp,
                lineHeight = 17.sp,
                color = KarikaUiColors.Muted,
                maxLines = 1
            )
        }
        KIcon(
            icon = vectorResource(Res.drawable.ic_k_chevron_right),
            tint = KarikaUiColors.Subtle,
            size = 18.dp
        )
    }
}
