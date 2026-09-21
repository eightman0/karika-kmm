package karika.distribucija.ba.ui.view.shop.profile.messages.overview

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.api.chatFileFrom
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessagesOverviewComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    conversation: ChatConversation,
) : CommonComponent(componentContext, stateHolder) {

    val conversationState = mutableStateOf(conversation)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()
    val newMessage = mutableStateOf("")

    private val _recipients = MutableStateFlow<List<ChatRecipient>>(emptyList())
    val recipients = _recipients.asStateFlow()
    var showAttachmentSheet = mutableStateOf(false)
    val attachment = mutableStateOf<Pair<String, ByteArray>?>(null)

    init {
        when {
            conversation.conversationId != null -> getMessages()
            conversation.axis == ChatAxis.VENDOR_CUSTOMER && conversation.counterpartId == null ->
                loadRecipients()

            else -> startConversation(conversation.counterpartId)
        }

        val job = scope.launch {
            stateHolder.messageHandler.threadReloadState.collect {
                if (conversationState.value.conversationId != null) {
                    getMessages()
                }
            }
        }

        lifecycle.doOnDestroy {
            job.cancel()
        }
    }

    private fun loadRecipients() {
        scope.launch {
            chatRepository.getRecipients(ChatAxis.VENDOR_CUSTOMER).collect { result ->
                if (result is ResultState.Success) {
                    _recipients.update { result.data }
                }
            }
        }
    }

    fun selectRecipient(recipient: ChatRecipient) {
        conversationState.value = conversationState.value.copy(
            counterpartId = recipient.counterpartId,
            counterpartName = recipient.name
        )
        startConversation(recipient.counterpartId)
    }

    private fun startConversation(counterpartId: Long?) {
        val axis = conversationState.value.axis ?: return
        scope.launch {
            chatRepository.startConversation(axis, counterpartId).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        conversationState.value = result.data
                        getMessages()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    private fun getMessages() {
        val conversationId = conversationState.value.conversationId ?: return
        markChatRead(conversationId)

        scope.launch {
            chatRepository.getMessages(conversationId).collect { result ->
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

    fun sendMessage() {
        val conversationId = conversationState.value.conversationId ?: return
        val files = attachment.value?.let { (name, bytes) -> listOf(chatFileFrom(name, bytes)) }
            ?: emptyList()

        scope.launch {
            chatRepository.sendMessage(conversationId, newMessage.value, files)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            newMessage.value = ""
                            attachment.value = null
                            getMessages()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun pickPhoto() {
        stateHolder.handler.pickPhoto { name, data ->
            attachment.value = Pair(name, data)
        }
    }

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            attachment.value = Pair(name, data)
        }
    }
}
