package karika.distribucija.ba.ui.view.main.profile.messages.vendor

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.profile.messages.admin.AdminMessagesComponent
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorMessagesComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    AdminMessagesComponent(componentContext, stateHolder) {
    init {
        iOScope.launch {
            stateHolder.vendorMessagesReloadState.collect {
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
}