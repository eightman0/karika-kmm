package karika.distribucija.ba.ui.view.salesrep.messages.internal

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.StaffThreadMessage
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesInternalConversationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val threadId: Long,
    val counterpartName: String
) : CommonComponent(componentContext, stateHolder) {

    private val salesRepository = SalesRepository()

    private val _messages = MutableStateFlow<List<StaffThreadMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        scope.launch {
            stateHolder.customerThreadPush.collect { threadId ->
                load()
            }
        }

        load()
    }

    private fun load() {
        markRead()
        scope.launch {
            salesRepository.getConversationMessages(threadId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _messages.value = result.data.items
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val msg = text.trim()
        if (msg.isBlank()) return
        scope.launch {
            salesRepository.sendConversationMessage(threadId, msg).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        load()
                    }
                    is ResultState.Error -> {
                        hideLoader()
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    private fun markRead() {
        scope.launch {
            salesRepository.markConversationRead(threadId).collect {
                stateHolder.refreshInternalMessages()
            }
        }
    }

    fun goBack() = salesRepBack()
}
