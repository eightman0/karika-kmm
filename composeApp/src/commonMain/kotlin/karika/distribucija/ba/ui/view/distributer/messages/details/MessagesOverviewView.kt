package karika.distribucija.ba.ui.view.distributer.messages.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.chatImage
import karika.distribucija.ba.domain.model.FileData
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MessagesOverviewView(component: MessagesOverviewComponent) {
    val comments = component.messages.collectAsState()
    val state = rememberLazyListState()
    val conversation by component.conversationState.asState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        YSpacer8()
        IconTextItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .onClick {
                    component.dashBack()
                },
            icon = vectorResource(Res.drawable.ic_arrow_back),
            iconColor = KarikaColors.Gray2,
            textColor = KarikaColors.Gray2,
            text = "Nazad na poruke",
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            iconPosition = FabPosition.Start
        )
        YSpacer8()
        SearchForVendor(component)
        Subject(component)
        LazyColumn(
            state = state,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = comments.value) { item ->
                MessageItem(item, component)
            }
        }
        EnterComment(component)
    }

    LoadingView1(component)
    LaunchedEffect(comments.value) {
        component.mainScope.launch {
            state.scrollToItem(comments.value.size)
        }
    }
}

@Composable
private fun Subject(component: MessagesOverviewComponent) {
    val subject = component.subject.asState()
    val conversation = component.conversationState.asState()
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Naslov",
            value = subject,
            placeholder = "Unesite naslov",
            imeAction = ImeAction.Next,
            enabled = conversation.value.subject == null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchForVendor(component: MessagesOverviewComponent) {
    val conversation = component.conversationState.asState()
    if (conversation.value.id != null || conversation.value.admin) {
        return
    }
    val searchText = mutableStateOf("").asState()
    val vendors by component.shops.collectAsState()
    val expand = mutableStateOf(false).asState()
    SearchBar(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        inputField = {
            KarikaTextField1(
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            expand.value = true
                            component.vendors(searchText.value, loadImmediately = true)
                        }
                    }
                    .fillMaxWidth(),
                title = "Primalac",
                value = searchText,
                placeholder = "Prtražite primalace",
                imeAction = ImeAction.Search,
                maxLines = 1,
                onValueChange = {
                    if (searchText.value.length > 2) {
                        expand.value = true
                        component.vendors(searchText.value)
                    }
                },
                enabled = conversation.value.id == null,
                trailingIcons = {
                    if (searchText.value.isNotEmpty()) {
                        Icon(
                            modifier = Modifier
                                .onClick {
                                    searchText.value = ""
                                    expand.value = true
                                    component.clear()
                                    component.vendors(searchText.value, loadImmediately = true)
                                }
                                .size(32.dp),
                            imageVector = vectorResource(Res.drawable.ic_tertiary),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                    }
                },
                doneAction = {
                    if (searchText.value.length > 2) {
                        expand.value = true
                        component.vendors(searchText.value)
                    }
                }
            )
        },
        expanded = expand.value,
        onExpandedChange = {},
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        colors = SearchBarDefaults.colors(
            containerColor = KarikaColors.White,
            dividerColor = KarikaColors.White
        ),
        shape = RoundedCornerShape(0.dp),
        windowInsets = WindowInsets(0.dp)
    ) {
        if (vendors.isEmpty()) {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "Nema rezultata za unijeti pojam '${searchText.value}'",
                color = KarikaColors.Primary,
                textSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W600
            )
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                vendors.forEach {
                    Box(
                        modifier = Modifier
                            .onClick {
                                expand.negate()
                                searchText.value = it.name ?: ""
                                conversation.value = conversation.value.copy(
                                    customerId = it.id,
                                    receiverName = it.name,
                                    senderName = it.name
                                )
                            }
                            .background(color = KarikaColors.Gray12)
                            .fillMaxWidth()
                    ) {
                        KarikaText(
                            modifier = Modifier
                                .padding(8.dp),
                            text = it.name,
                            color = KarikaColors.Black,
                            textSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W600
                        )
                    }
                    YSpacer8()
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
    val subject = component.subject.asState()
    val enableButton = remember(comment, conversation, subject) {
        derivedStateOf {
            comment.value.isNotEmpty() &&
                    (conversation.value.customerId != null || conversation.value.receiverId == "0") &&
                    subject.value.isNotEmpty()

        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaTextField2(
            modifier = Modifier
                .weight(1f),
            value = comment,
            placeholder = "Napiši komentar",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            trailingIcons = {
                Icon(
                    modifier = Modifier
                        .onClick {
                            component.pickFile()
                        },
                    imageVector = vectorResource(Res.drawable.ic_attachment),
                    tint = KarikaColors.Gray2,
                    contentDescription = ""
                )
            }
        )
        SecondaryButtonFilled(
            modifier = Modifier
                .height(50.dp),
            title = "Pošalji",
            enabled = enableButton.value
        ) {
            keyboardController?.hide()
            component.sendMessage()
        }
    }
}

@Composable
fun MessageItem(message: Message, component: MessagesOverviewComponent) {
    if (message.isMine(component.stateHolder.vendorDetails.value.entityId)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .background(
                        color = KarikaColors.Blue,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.End
            ) {
                message.images
                    ?.takeIf { image -> image.isNotEmpty() }
                    ?.let {
                        Json.decodeFromString<FileData>(it).filename?.firstOrNull()?.let { image ->
                            KarikaImage(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(150.dp)
                                    .onClick {
                                        component.showImagePreview(chatImage(image))
                                    },
                                model = chatImage(image),
                                contentScale = ContentScale.Inside
                            )
                        }
                    }
                if (!message.message.isNullOrEmpty()) {
                    if (message.message?.contains("<") == true) {
                        HtmlTextWithStyles(
                            modifier = Modifier
                                .padding(16.dp),
                            html = message.message ?: "",
                            textColor = KarikaColors.White,
                            background = KarikaColors.MineMessage
                        )
                    } else {
                        KarikaText(
                            modifier = Modifier
                                .padding(16.dp),
                            text = message.message,
                            color = KarikaColors.White,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = message.createdAt,
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .background(
                        color = KarikaColors.Primary,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp),
                    text = component.conversationState.value.senderName,
                    color = KarikaColors.White,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                message.images
                    ?.takeIf { image -> image.isNotEmpty() }
                    ?.let {
                        Json.decodeFromString<FileData>(it).filename?.firstOrNull()?.let { image ->
                            KarikaImage(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(150.dp)
                                    .onClick {
                                        component.showImagePreview(chatImage(image))
                                    },
                                model = chatImage(image),
                                contentScale = ContentScale.Inside
                            )
                        }
                    }
                if (!message.message.isNullOrEmpty()) {
                    if (message.message?.contains("<") == true) {
                        HtmlTextWithStyles(
                            modifier = Modifier
                                .padding(16.dp),
                            html = message.message ?: "",
                            textColor = KarikaColors.White,
                            background = KarikaColors.NotMineMessage
                        )
                    } else {
                        KarikaText(
                            modifier = Modifier
                                .padding(16.dp),
                            text = message.message,
                            color = KarikaColors.White,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }

                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = message.createdAt,
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    }
}
