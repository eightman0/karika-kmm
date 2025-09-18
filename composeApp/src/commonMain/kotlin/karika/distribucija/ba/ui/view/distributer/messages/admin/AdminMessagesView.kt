package karika.distribucija.ba.ui.view.distributer.messages.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick

@Composable
fun AdminMessagesView(component: AdminMessagesComponent) {
    val state = rememberLazyListState()
    val messages = component.messages.collectAsState()


    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Poruke admina",
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )
            YSpacer16()
           /* Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBoxBorder(
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f),
                    borderShape = 100.dp,
                    onValueChange = {
                        component.searchText.value = it
                    },
                    onClose = {
                        component.searchText.value = ""
                        component.loadNextPage(true)
                    },
                    onSearchExecute = {
                        component.loadNextPage(true)
                    },
                    placeholder = "Pretraži poruke...",
                    preselected = component.searchText.value
                )
            }*/
        }
        items(items = messages.value) {
            MessageItem(it, component)
        }
    }
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        SecondaryButtonFilled(
            modifier = Modifier
                .height(47.dp),
            title = "Pošalji novu poruku",
            fontWeight = FontWeight.W600,
            textSize = 18.sp
        ) {
            component.navigateToMessagesOverview(
                Conversation(
                    receiverId = "0",
                    receiverName = "Karika Distribucija",
                    admin = true
                )
            )
        }
    }
    LoadingView1(component)

    LaunchedEffect(Unit) {
        component.loadNextPage(true)
    }
}

@Composable
private fun MessageItem(item: Conversation, component: AdminMessagesComponent) {
    Column(
        modifier = Modifier
            .onClick {
                component.navigateToMessagesOverview(item)
            }
            .background(color = KarikaColors.Gray12, shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                color = KarikaColors.Gray2,
                fontWeight = if (item.isRead()) FontWeight.W400 else FontWeight.W700,
                textSize = 16.sp,
                text = item.senderName()
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                fontWeight = if (item.isRead()) FontWeight.W400 else FontWeight.W700,
                textSize = 12.sp,
                text = item.createdAt
            )
        }
        KarikaText(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            text = item.subject
        )
    }
}