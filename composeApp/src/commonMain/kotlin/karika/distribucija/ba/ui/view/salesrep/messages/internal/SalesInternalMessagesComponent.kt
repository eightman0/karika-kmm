package karika.distribucija.ba.ui.view.salesrep.messages.internal

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.StaffThread
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInternalMessagesComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    private val _threads = MutableStateFlow<List<StaffThread>>(emptyList())
    val threads = _threads.asStateFlow()

    init {
        load()
        scope.launch {
            stateHolder.refreshInternalMessages.collect { load() }
        }
    }

    fun openConversation(thread: StaffThread) {
        salesRepPush(
            SalesRepConfig.InternalConversation(
                threadId = thread.threadId,
                counterpartName = thread.counterpartName
            )
        )
    }

    fun openNewMessage() {
        salesRepPush(SalesRepConfig.InternalNewMessage)
    }

    fun refresh() = load()

    private fun load() {
        scope.launch {
            salesRepository.listConversations().collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _threads.value = result.data.items
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }
}
