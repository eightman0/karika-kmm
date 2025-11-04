package karika.distribucija.ba.ui.view.main.profile.messages.vendor

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.profile.messages.admin.AdminMessagesComponent
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorMessagesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    AdminMessagesComponent(componentContext, stateHolder) {

    override fun init() {
        iOScope.launch {
            stateHolder.messageHandler.vendorMessagesReloadState.collect {
                loadNextPage()
            }
        }
    }

    override fun loadNextPage(reset: Boolean) {
        iOScope.launch {
            messagesRepository.messages(admin = false)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _messages.update { result.data }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    override fun navigateToMessagesOverview(item: Conversation) {
        super.navigateToMessagesOverview(item)
        if (item.isRead()) {
            return
        }
        iOScope.launch {
            messagesRepository.markAsRead(item.id)
                .collect {
                    if (item.admin) {
                        stateHolder.messageHandler.reloadAdminMessages()
                    } else {
                        stateHolder.messageHandler.reloadVendorMessages()
                    }
                }
        }
        stateHolder.customerNotificationHandler.notificationReceived()
    }
}