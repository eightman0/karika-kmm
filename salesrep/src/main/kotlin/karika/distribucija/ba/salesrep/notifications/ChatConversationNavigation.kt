package karika.distribucija.ba.salesrep.notifications

import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.api.ChatRepository
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.launch

/** Mirrors composeApp's PushHandler.navigateToChatConversationSalesRep(): looks the conversation
 * up (GET /V1/chat/conversations/{id}) and opens the admin, internal or customer conversation
 * screen by its axis. Nothing opens when the lookup fails. */
fun LifecycleOwner.openChatConversation(
    navController: NavController,
    conversationId: Long,
    navOptions: NavOptions? = null
) {
    lifecycleScope.launch {
        ChatRepository().getConversation(conversationId).collect { result ->
            if (result !is ResultState.Success) return@collect
            val destination = when (result.data.axis) {
                ChatAxis.VENDOR_ADMIN -> R.id.adminConversationFragment
                ChatAxis.STAFF -> R.id.internalConversationFragment
                else -> R.id.customerConversationFragment
            }
            navController.navigate(
                destination,
                bundleOf(
                    "conversationId" to (result.data.conversationId ?: conversationId),
                    "counterpartName" to result.data.counterpartName
                ),
                navOptions
            )
        }
    }
}
