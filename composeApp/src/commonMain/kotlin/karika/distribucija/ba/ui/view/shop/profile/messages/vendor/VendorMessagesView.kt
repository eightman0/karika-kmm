package karika.distribucija.ba.ui.view.shop.profile.messages.vendor

import androidx.compose.runtime.Composable
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.ui.view.shop.profile.messages.admin.MessagesScreen

@Composable
fun VendorMessagesView(component: VendorMessagesComponent) {
    MessagesScreen(
        title = "Poruke dobavljača",
        component = component
    ) {
        component.navigateToMessagesOverview(
            ChatConversation(
                conversationId = null,
                axis = ChatAxis.VENDOR_CUSTOMER,
                counterpartName = "Nova poruka"
            )
        )
    }
}
