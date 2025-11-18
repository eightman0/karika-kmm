package karika.distribucija.ba.ui.view.main.profile.messages.overview

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessagesOverviewComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    conversation: Conversation,
) : CommonComponent(componentContext, stateHolder) {

    val conversationState = mutableStateOf(conversation)
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    val newMessage = mutableStateOf("")
    val subject = mutableStateOf(conversation.subject ?: "")

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors = _vendors.asStateFlow()
    var showAttachmentSheet = mutableStateOf(false)

    init {
        if (conversation.createdAt == null) {
            vendors("", true)
        }

        iOScope.launch {
            stateHolder.messageHandler.adminMessagesReloadState.collect {
                getMessages()
            }
            stateHolder.messageHandler.vendorMessagesReloadState.collect {
                getMessages()
            }
        }
    }

    private fun getMessages(threadId: String? = conversationState.value.id) {
        if (threadId == null) {
            return
        }

        iOScope.launch {
            messagesRepository.get(
                threadId = threadId,
                admin = conversationState.value.admin()
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _messages.update {
                            result.data.firstOrNull()?.messages?.firstOrNull() ?: emptyList()
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun sendMessage(attachment: ByteArray? = null) {
        iOScope.launch {
            messagesRepository.send(
                SendMessageRequest(
                    sendToAdmin = conversationState.value.admin(),
                    message = newMessage.value,
                    subject = subject.value,
                    receiverId = conversationState.value.receiverId(),
                    threadId = conversationState.value.id?.toIntOrNull(),
                    image = attachment
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        if (conversationState.value.id == null) {
                            stateHolder.messageHandler.reloadAdminMessages()
                            stateHolder.messageHandler.reloadVendorMessages()
                            conversationState.value = conversationState.value.copy(
                                id = result.data.threadId,
                                subject = subject.value
                            )
                        }
                        getMessages(threadId = result.data.threadId)
                        newMessage.value = ""
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun vendors(searchText: String, loadImmediately: Boolean = false) {
        if (!loadImmediately && searchText.length < 3) {
            return
        }
        iOScope.launch {
            messagesRepository.vendors(
                searchText
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _vendors.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun clear() {
        _vendors.update { emptyList() }
    }

    fun pickPhoto() {
        stateHolder.handler.pickPhoto { name, data ->
            sendMessage(data)
        }
    }

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            sendMessage(data)
        }
    }

    fun getVendorName(): String? {
        return when {
            conversationState.value.sender == "customer" -> conversationState.value.senderName
            else -> conversationState.value.receiverName
        }
    }

}