package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.ChatFile
import karika.distribucija.ba.domain.model.ChatMessage
import karika.distribucija.ba.domain.model.ChatMessageSearchResults
import karika.distribucija.ba.domain.model.ChatRecipient
import karika.distribucija.ba.domain.model.ChatSendBody
import karika.distribucija.ba.domain.model.ChatSendRequest
import karika.distribucija.ba.domain.model.ChatStartBody
import karika.distribucija.ba.domain.model.ChatStartRequest
import karika.distribucija.ba.domain.model.ChatStatus
import karika.distribucija.ba.domain.model.ChatUnreadCount
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.io.encoding.Base64

internal class ChatApi {

    /** GET /V1/chat/status */
    suspend fun getStatus(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/status"))
    }

    /** GET /V1/chat/conversations */
    suspend fun getConversations(
        axis: ChatAxis,
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/conversations")) {
            parameter("page", page)
            parameter("page_size", pageSize)
            parameter("axis", axis.wireValue())
            if (unreadOnly) {
                parameter("unread_only", true)
            }
        }
    }

    /** POST /V1/chat/conversations */
    suspend fun startConversation(
        axis: ChatAxis,
        counterpartId: Long? = null
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("chat/conversations")) {
            setBody(ChatStartBody(ChatStartRequest(axis, counterpartId)))
        }
    }

    /** GET /V1/chat/conversations/{id} */
    suspend fun getConversation(conversationId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/conversations/$conversationId"))
    }

    /** GET /V1/chat/conversations/{id}/messages */
    suspend fun getMessages(
        conversationId: Long,
        beforeId: Long? = null,
        limit: Int = 100
    ): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/conversations/$conversationId/messages")) {
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
        HttpClientProvider.client.post(url("chat/conversations/$conversationId/messages")) {
            setBody(ChatSendBody(ChatSendRequest(message, files)))
        }
    }

    /** POST /V1/chat/conversations/{id}/read */
    suspend fun markRead(conversationId: Long): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.post(url("chat/conversations/$conversationId/read"))
    }

    /** GET /V1/chat/unread-count */
    suspend fun getUnreadCount(): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/unread-count"))
    }

    /** GET /V1/chat/recipients */
    suspend fun getRecipients(axis: ChatAxis? = null): Result<HttpResponse> = runCatching {
        HttpClientProvider.client.get(url("chat/recipients")) {
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

fun mimeFromFilename(filename: String): String {
    return when (filename.substringAfterLast('.', "").lowercase()) {
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
}

fun chatFileFrom(filename: String, bytes: ByteArray): ChatFile {
    return ChatFile(
        filename = filename,
        mime = mimeFromFilename(filename),
        content = Base64.encode(bytes)
    )
}

class ChatRepository internal constructor() {

    fun getStatus(): Flow<ResultState<ChatStatus>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getStatus().getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatStatus>()))
                return@flow
            }
            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getConversations(
        axis: ChatAxis,
        page: Int = 1,
        pageSize: Int = 20,
        unreadOnly: Boolean = false
    ): Flow<ResultState<ChatConversationSearchResults>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi()
                .getConversations(axis, page, pageSize, unreadOnly)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatConversationSearchResults>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun startConversation(
        axis: ChatAxis,
        counterpartId: Long? = null
    ): Flow<ResultState<ChatConversation>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi()
                .startConversation(axis, counterpartId)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatConversation>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
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

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
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
            val response = ChatApi()
                .getMessages(conversationId, beforeId, limit)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatMessageSearchResults>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
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
            val response = ChatApi()
                .sendMessage(conversationId, message, files)
                .getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatMessage>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
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
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getUnreadCount(): Flow<ResultState<ChatUnreadCount>> = flow {
        emit(ResultState.Loading)
        try {
            val response = ChatApi().getUnreadCount().getOrNoInternet()

            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<ChatUnreadCount>()))
                return@flow
            }

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
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

            emit(ResultState.Error("Došlo je do greške. Pokušajte ponovo!"))
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}
