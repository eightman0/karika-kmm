package karika.distribucija.ba.ui.view.main.profile.messages.overview

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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MessagesOverviewView(component: MessagesOverviewComponent) {
    val comments = component.messages.collectAsState()
    val state = rememberLazyListState()
    val conversation by component.conversationState.asState()
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack(conversation.receiverName ?: "Nova poruka") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
    }

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
    val vendors by component.vendors.collectAsState()
    val expand = mutableStateOf(false).asState()
    SearchBar(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        inputField = {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Dobavljač",
                value = searchText,
                placeholder = "Prtražite dobavljača",
                imeAction = ImeAction.Search,
                maxLines = 1,
                onValueChange = {
                    //component.vendors(subject.value)
                },
                enabled = conversation.value.id == null,
                trailingIcons = {
                    if (searchText.value.isNotEmpty()) {
                        Icon(
                            modifier = Modifier
                                .onClick {
                                    searchText.value = ""
                                    expand.value = false
                                    component.clear()
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
            vendors.forEach {
                Box(
                    modifier = Modifier
                        .onClick {
                            expand.negate()
                            searchText.value = it.name()
                            conversation.value = conversation.value.copy(
                                vendorId = it.entityId.toString(),
                                receiverName = it.name(),
                                senderName = it.name()
                            )
                        }
                        .background(color = KarikaColors.Gray12)
                        .fillMaxWidth()
                ) {
                    KarikaText(
                        modifier = Modifier
                            .padding(8.dp),
                        text = it.name(),
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

@Composable
private fun EnterComment(component: MessagesOverviewComponent) {
    val comment = component.newMessage.asState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val conversation = component.conversationState.asState()
    val subject = component.subject.asState()
    val enableButton = remember(comment, conversation, subject) {
        derivedStateOf {
            comment.value.isNotEmpty() &&
                    (conversation.value.vendorId != null || conversation.value.receiverId == "0") &&
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
            imeAction = ImeAction.Done
        )
        PrimaryButtonFilled(
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
    if (message.isVendorMessage()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .background(
                        color = KarikaColors.MineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.End
            ) {
                /*KarikaText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = message.message,
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )*/
                HtmlTextWithStyles(
                    modifier = Modifier
                        .padding(16.dp),
                    html = message.message ?: "",
                    textColor = KarikaColors.White,
                    background = KarikaColors.MineMessage
                )
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
                        color = KarikaColors.NotMineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                /*KarikaText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = message.message,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )*/
                HtmlTextWithStyles(
                    modifier = Modifier
                        .padding(16.dp),
                    html = message.message ?: "",
                    textColor = KarikaColors.Gray2,
                    background = KarikaColors.NotMineMessage
                )
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = message.createdAt,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    }
}
