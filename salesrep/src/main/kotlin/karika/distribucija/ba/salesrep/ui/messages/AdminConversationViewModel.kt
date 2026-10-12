package karika.distribucija.ba.salesrep.ui.messages

import androidx.lifecycle.SavedStateHandle

/** Mirrors composeApp's SalesAdminConversationComponent.kt - the same Chat V2 load/send mechanics
 * as [CustomerConversationViewModel]; only the conversation (vendor_admin axis) differs. */
class AdminConversationViewModel(savedStateHandle: SavedStateHandle) :
    CustomerConversationViewModel(savedStateHandle)
