package karika.distribucija.ba.ui.view.distributer.messages.details

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.api.chatFileFrom
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
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

    val attachment = mutableStateOf<Pair<String, ByteArray>?>(null)
    val showAttachmentSheet = mutableStateOf(false)

    init {
        when {
            conversation.conversationId != null -> getMessages()
            isPickerAxis(conversation.axis) && conversation.counterpartId == null ->
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

    private fun isPickerAxis(axis: ChatAxis?) =
        axis == ChatAxis.VENDOR_CUSTOMER || axis == ChatAxis.STAFF

    private fun loadRecipients() {
        val axis = conversationState.value.axis ?: return
        scope.launch {
            chatRepository.getRecipients(axis).collect { result ->
                if (result is ResultState.Success) {
                    _recipients.update { result.data }
                }
            }
        }
    }

    fun isMine(message: ChatMessage, me: VendorOperationsMe): Boolean {
        if (conversationState.value.axis != ChatAxis.STAFF) {
            return message.isFromVendor()
        }
        val myRefId = if (me.isVendorOwner) me.vendorId ?: 0L else me.employeeId ?: 0L
        return message.senderRefId == myRefId
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
        markChatReadVendor(conversationId)

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

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            attachment.value = Pair(name, data)
        }
    }

    fun pickPhoto() {
        stateHolder.handler.pickPhoto { name, data ->
            attachment.value = Pair(name, data)
        }
    }
}
