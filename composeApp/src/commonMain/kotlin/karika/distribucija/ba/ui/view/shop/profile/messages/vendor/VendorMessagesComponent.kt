package karika.distribucija.ba.ui.view.shop.profile.messages.vendor

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.shop.profile.messages.admin.AdminMessagesComponent
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorMessagesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    AdminMessagesComponent(componentContext, stateHolder) {

    override fun init() {
        val job = scope.launch {
            stateHolder.messageHandler.vendorMessagesReloadState.collect {
                loadNextPage()
            }
        }

        lifecycle.doOnDestroy {
            job.cancel()
        }
    }

    override fun loadNextPage(reset: Boolean) {
        scope.launch {
            chatRepository.getConversations(axis = ChatAxis.VENDOR_CUSTOMER, pageSize = 100)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _messages.update { result.data.items }
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }
}
