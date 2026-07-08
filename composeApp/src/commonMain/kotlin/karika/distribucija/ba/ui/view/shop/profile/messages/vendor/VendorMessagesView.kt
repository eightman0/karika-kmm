package karika.distribucija.ba.ui.view.shop.profile.messages.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.view.shop.profile.messages.admin.MessageList

@Composable
fun VendorMessagesView(component: VendorMessagesComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Poruke dobavljača") {
                component.appBack()
            }
        },
        floatingActionButton = {
            PrimaryButtonFilled(
                modifier = Modifier
                    .height(47.dp),
                title = "Pošalji novu poruku",
                fontWeight = FontWeight.W600,
                textSize = 18.sp
            ) {
                component.navigateToMessagesOverview(
                    Conversation(
                        receiverName = "Nova poruka",
                        senderName = "Nova poruka",
                        admin = false
                    )
                )
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MessageList(component)
        }
    }
}