package karika.distribucija.ba.salesrep.api

import android.util.Base64
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.model.ChatAxis
import karika.distribucija.ba.salesrep.model.ChatConversation
import karika.distribucija.ba.salesrep.model.ChatConversationSearchResults
import karika.distribucija.ba.salesrep.model.ChatFile
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.model.ChatMessageSearchResults
import karika.distribucija.ba.salesrep.model.ChatRecipient
import karika.distribucija.ba.salesrep.model.ChatSendBody
import karika.distribucija.ba.salesrep.model.ChatSendRequest
import karika.distribucija.ba.salesrep.model.ChatStartBody
import karika.distribucija.ba.salesrep.model.ChatStartRequest
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Ported from composeApp's domain/api/ChatApi.kt - Chat V2 (`/V1/chat/...`), the only API the
 * customer, admin and internal message screens use (not the legacy `/V1/mobile/message/...` or
 * `/V1/vendor-operations/conversations*`). */
internal class ChatApi {

    /** GET /V1/chat/conversations */
    suspend fun getConversations(
        axis: ChatAxis,
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("chat/conversations")) {
            parameter("page", page)
            parameter("page_size", pageSize)
            parameter("axis", axis.wireValue())
            if (unreadOnly) {
                parameter("unread_only", true)
            }
        }
    }

    /** POST /V1/chat/conversations */
    suspend fun startConversation(axis: ChatAxis, counterpartId: Long? = null): Result<HttpResponse> =
        runCatching {
            HttpClientProvider.client.post(HttpClientProvider.url("chat/conversations")) {
                setBody(ChatStartBody(ChatStartRequest(axis, counterpartId)))
            }
        }

    /** GET /V1/chat/conversations/{id} */
    suspend fun getConversation(conversationId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("chat/conversations/$conversationId"))
    }

    /** GET /V1/chat/conversations/{id}/messages */
    suspend fun getMessages(
        conversationId: Long,
        beforeId: Long? = null,
        limit: Int = 100
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("chat/conversations/$conversationId/messages")) {
            beforeId?.let { parameter("before_id", it) }
            parameter("limit", limit)
        }
    }

    /** POST /V1/chat/conversations/{id}/messages */
    suspend fun sendMessage(
        conversationId: Long,
        message: String,
        files: List<ChatFile> = emptyList()
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("chat/conversations/$conversationId/messages")) {
            setBody(ChatSendBody(ChatSendRequest(message, files)))
        }
    }

    /** POST /V1/chat/conversations/{id}/read */
    suspend fun markRead(conversationId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(HttpClientProvider.url("chat/conversations/$conversationId/read"))
    }

    /** GET /V1/chat/recipients */
    suspend fun getRecipients(axis: ChatAxis? = null): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(HttpClientProvider.url("chat/recipients")) {
            axis?.let { parameter("axis", it.wireValue()) }
        }
    }
}

private fun ChatAxis.wireValue(): String = when (this) {
    ChatAxis.VENDOR_CUSTOMER -> "vendor_customer"
    ChatAxis.CUSTOMER_ADMIN -> "customer_admin"
    ChatAxis.VENDOR_ADMIN -> "vendor_admin"
    ChatAxis.STAFF -> "staff"
}

private fun mimeFromFilename(filename: String): String =
    when (filename.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "csv" -> "text/csv"
        else -> "application/octet-stream"
    }

/** A picked attachment as Chat V2's base64 `files[]` entry (composeApp's chatFileFrom()). */
fun chatFileFrom(filename: String, bytes: ByteArray): ChatFile = ChatFile(
    filename = filename,
    mime = mimeFromFilename(filename),
    content = Base64.encodeToString(bytes, Base64.NO_WRAP)
)

/** The single pending attachment of a message screen as the list sendMessage() takes. */
fun Pair<String, ByteArray>?.toChatFiles(): List<ChatFile> =
    this?.let { (name, bytes) -> listOf(chatFileFrom(name, bytes)) } ?: emptyList()

class ChatRepository internal constructor() {

    companion object {
        private const val TAG = "ChatRepository"
        private const val GENERIC_ERROR = "Došlo je do greške. Pokušajte ponovo!"
    }

    fun getConversations(
        axis: ChatAxis,
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false
    ): Flow<ResultState<ChatConversationSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getConversations(axis, page, pageSize, unreadOnly).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatConversationSearchResults>()))
                return@flow
            }
            emit(ResultState.Error(GENERIC_ERROR))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun startConversation(axis: ChatAxis, counterpartId: Long? = null): Flow<ResultState<ChatConversation>> =
        flow {
            emit(ResultState.Loading)
            try {
                val response = ChatApi().startConversation(axis, counterpartId).getOrNoInternet()
                if (response.status == HttpStatusCode.OK) {
                    emit(ResultState.Success(response.body<ChatConversation>()))
                    return@flow
                }
                emit(ResultState.Error(GENERIC_ERROR))
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Network call failed", e)
                emit(ResultState.Error(e.message))
            }
        }.flowOn(Dispatchers.Default)

    fun getConversation(conversationId: Long): Flow<ResultState<ChatConversation>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getConversation(conversationId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatConversation>()))
                return@flow
            }
            emit(ResultState.Error(GENERIC_ERROR))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getMessages(
        conversationId: Long,
        beforeId: Long? = null,
        limit: Int = 100
    ): Flow<ResultState<ChatMessageSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getMessages(conversationId, beforeId, limit).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatMessageSearchResults>()))
                return@flow
            }
            emit(ResultState.Error(GENERIC_ERROR))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun sendMessage(
        conversationId: Long,
        message: String,
        files: List<ChatFile> = emptyList()
    ): Flow<ResultState<ChatMessage>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().sendMessage(conversationId, message, files).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatMessage>()))
                return@flow
            }
            emit(ResultState.Error(GENERIC_ERROR))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun markRead(conversationId: Long): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().markRead(conversationId).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
                return@flow
            }
            emit(ResultState.Error(""))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getRecipients(axis: ChatAxis? = null): Flow<ResultState<List<ChatRecipient>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getRecipients(axis).getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<List<ChatRecipient>>()))
                return@flow
            }
            emit(ResultState.Error(GENERIC_ERROR))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Network call failed", e)
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
