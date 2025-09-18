package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.Conversation
import karika.distribucija.ba.domain.model.MessagesCount
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SendMessageRequest
import karika.distribucija.ba.domain.model.SendMessageResponse
import karika.distribucija.ba.domain.model.Shop
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class MessagesApi {
    suspend fun messages(
        admin: Boolean = true,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/message/list?pageSize=1000&curPage=1&admin=$admin")
        )
    }

    suspend fun get(
        threadId: String?,
        admin: Boolean = true,
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/message/thread?threadId=$threadId&admin=$admin")
        )
    }

    suspend fun getMessageUnread(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("mobile/message/count")
        )
    }

    suspend fun markAsRead(threadId: String?): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/message/markAsRead?threadId=$threadId")
        )
    }

    suspend fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            when {
                filterBy.isNotEmpty() ->
                    url("mobile/vendors?searchCriteria[filterGroups][0][filters][0][field]=$filterBy&searchCriteria[filterGroups][0][filters][0][value]=$filterValue&searchCriteria[filterGroups][0][filters][0][conditionType]=equals&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                searchText.isNotEmpty() ->
                    url("mobile/vendors?searchCriteria[filterGroups][0][filters][0][field]=public_name&searchCriteria[filterGroups][0][filters][0][value]=$searchText&searchCriteria[filterGroups][0][filters][0][conditionType]=like&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                else ->
                    url("mobile/vendors?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")
            }
        )
    }

    suspend fun shops(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            when {
                filterBy.isNotEmpty() ->
                    url("mobile/vendor/customers?searchCriteria[filterGroups][0][filters][0][field]=$filterBy&searchCriteria[filterGroups][0][filters][0][value]=$filterValue&searchCriteria[filterGroups][0][filters][0][conditionType]=equals&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                searchText.isNotEmpty() ->
                    url("mobile/vendor/customers?searchCriteria[filterGroups][0][filters][0][field]=b2b_pravno_lice&searchCriteria[filterGroups][0][filters][0][value]=$searchText&searchCriteria[filterGroups][0][filters][0][conditionType]=like&searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")

                else ->
                    url("mobile/vendor/customers?searchCriteria[pageSize]=$pageSize&searchCriteria[currentPage]=$currentPage")
            }
        )
    }

    suspend fun send(
        message: SendMessageRequest
    ): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("mobile/message/send")
        ) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("send_to_admin", message.sendToAdmin)
                        append("message", message.message)
                        append("subject", message.subject ?: "")
                        message.receiverId?.let {
                            append("receiver_id", it)
                        }
                        message.threadId?.let {
                            append("thread_id", it)
                        }
                        message.image?.let {
                            append("files[]", it, Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"${Clock.System.now().toEpochMilliseconds()}.png\""
                                )
                            })
                        }
                    }.withLog(),
                    boundary = "WebAppBoundary"
                )
            )
        }
    }
}

class MessagesRepository internal constructor() {
    fun messages(
        admin: Boolean = true,
    ): Flow<ResultState<List<Conversation>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .messages(admin).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun get(
        threadId: String?,
        admin: Boolean = true,
    ): Flow<ResultState<List<Conversation>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .get(threadId, admin).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun vendors(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Flow<ResultState<List<Vendor>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .vendors(searchText, pageSize, currentPage, filterBy, filterValue)
                .getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun shops(
        searchText: String = "",
        pageSize: Int = 10000,
        currentPage: Int = 1,
        filterBy: String = "",
        filterValue: String = "",
    ): Flow<ResultState<List<Shop>>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .shops(searchText, pageSize, currentPage, filterBy, filterValue)
                .getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun send(
        message: SendMessageRequest
    ): Flow<ResultState<SendMessageResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .send(message).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun messageUnreadCount(): Flow<ResultState<MessagesCount>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .getMessageUnread().getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
                return@flow
            }

            emit(ResultState.Error(response?.bodyAsText() ?: ""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }

    fun markAsRead(id: String?): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = MessagesApi()
                .markAsRead(id).getOrNull()

            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(""))
                return@flow
            }

            emit(ResultState.Error(""))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: ""))
        }
    }
}

fun List<PartData>.withLog(): List<PartData> {
    also {
        val json = buildJsonObject {
            it.filterIsInstance<PartData.FormItem>()
                .map { it1 -> it1 }.forEach { it2 ->
                    put(it2.name ?: "", it2.value)
                }
        }
        println("RequestBody: $json")
    }
    return this
}