package karika.distribucija.ba.ui.view.salesrep.messages.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import karika.distribucija.ba.ui.view.salesrep.messages.SalesConversationList
import karika.distribucija.ba.ui.view.salesrep.messages.salesStaffRole

@Composable
fun SalesInternalMessagesView(component: SalesInternalMessagesComponent) {
    val threads by component.conversations.collectAsState()

    SalesConversationList(
        conversations = threads,
        onOpen = { component.openConversation(it) },
        onNewMessage = { component.openNewMessage() },
        newMessageText = "Nova interna poruka",
        emptyTitle = "Još nema internih poruka",
        emptyMessage = "Ovdje će se prikazati vaše interne poruke s kolegama.",
        subtitle = { salesStaffRole(it.counterpartType) }
    )
}
