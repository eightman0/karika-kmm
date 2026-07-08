package karika.distribucija.ba.ui.view.shop.profile.order.comments

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.openPdf
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    private val order: Order
) : CommonComponent(componentContext, stateHolder) {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()
    val newComment = mutableStateOf("")

    init {
        getComments()

        scope.launch {
            stateHolder.customerSpecificHandler.refreshOrders.collect {
                getComments()
            }
        }
    }

    private fun getComments() {
        scope.launch {
            orderRepository.comments(
                orderId = order.orderId,
                order.vendorId?.toString()
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        hideLoader()
                        _comments.update { result.data }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun sendComment() {
        scope.launch {
            orderRepository.sendComment(
                orderId = order.orderId,
                vendorId = order.vendorId.toString(),
                comment = newComment.value
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        getComments()
                        newComment.value = ""
                        stateHolder.customerSpecificHandler.refreshOrders()
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
            scope.launch {
                orderRepository.sendBill(
                    orderId = order.orderId ?: return@launch,
                    vendorId = order.vendorId.toString(),
                    comment = newComment.value,
                    attachment = data,
                    filename = name
                ).collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            getComments()
                            newComment.value = ""
                            stateHolder.customerSpecificHandler.refreshOrders()
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

    fun downloadReceipt(it: File) {
        //stateHolder.filePicker.downloadFile(
        //    it.name ?: "",
        //    it.type ?: "",
        //    commentAttachment(it.url)
        //)
        openPdf(imageUrl(it.url))
    }
}