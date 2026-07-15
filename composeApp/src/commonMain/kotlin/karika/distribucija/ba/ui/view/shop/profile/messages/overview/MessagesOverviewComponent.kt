package karika.distribucija.ba.ui.view.shop.profile.messages.overview

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import karika.distribucija.ba.domain.HttpClientProvider.chatImage
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.Message
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.openPdf
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
    val attachment = mutableStateOf<Pair<String, ByteArray>?>(null)

    init {
        if (conversation.createdAt == null) {
            vendors("", true)
        }

        val job = scope.launch {
            stateHolder.messageHandler.threadReloadState.collect {
                getMessages()
            }
        }

        lifecycle.doOnDestroy {
            job.cancel()
        }
    }

    private fun getMessages(threadId: String? = conversationState.value.id) {
        markAsReadMessage(threadId, conversationState.value.admin)

        if (threadId == null) {
            return
        }

        scope.launch {
            messagesRepository.get(
                threadId = threadId,
                admin = conversationState.value.admin
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _messages.update {
                            result.data.firstOrNull()?.messages?.firstOrNull() ?: emptyList()
                        }
                        result.data.firstOrNull()?.let {
                            conversationState.value = conversationState.value.copy(
                                id = it.id,
                                subject = it.subject,
                                vendorId = it.vendorId,
                                customerId = it.customerId,
                                senderName = it.senderName,
                                receiverName = it.receiverName,
                                senderId = it.senderId,
                                sender = it.sender
                            )
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

    fun sendMessage() {
        scope.launch {
            messagesRepository.send(
                SendMessageRequest(
                    sendToAdmin = conversationState.value.admin(),
                    message = newMessage.value,
                    subject = subject.value,
                    receiverId = conversationState.value.receiverId(),
                    threadId = conversationState.value.id?.toIntOrNull(),
                    file = attachment.value
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        if (conversationState.value.id == null) {
                            conversationState.value = conversationState.value.copy(
                                id = result.data.threadId,
                                subject = subject.value
                            )
                        }
                        getMessages(threadId = result.data.threadId)
                        newMessage.value = ""
                        attachment.value = null
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
        scope.launch {
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
            attachment.value = Pair(name, data)
        }
    }

    fun pickFile() {
        stateHolder.handler.pickFile { name, data ->
            attachment.value = Pair(name, data)
        }
    }

    fun getVendorName(): String? {
        return when {
            conversationState.value.senderId == "0" -> conversationState.value.senderName
            conversationState.value.sender == "vendor" -> conversationState.value.senderName
            else -> conversationState.value.receiverName
        }
    }
}