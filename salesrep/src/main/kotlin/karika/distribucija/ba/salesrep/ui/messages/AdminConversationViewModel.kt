package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.SavedStateHandle

/** Mirrors composeApp's SalesAdminConversationComponent.kt - same load/send mechanics as
 * [CustomerConversationViewModel], scoped to admin=true. */
class AdminConversationViewModel(savedStateHandle: SavedStateHandle) :
    CustomerConversationViewModel(savedStateHandle) {
    override val admin: Boolean = true
}
