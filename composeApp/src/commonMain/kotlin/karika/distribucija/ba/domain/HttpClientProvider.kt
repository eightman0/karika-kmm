package karika.distribucija.ba.domain

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import karika.distribucija.ba.ui.common.getEnvPrefix
import kotlinx.serialization.json.Json

object HttpClientProvider {
    private val HOST = "https://${getEnvPrefix()}karika.ba/magento"
    private val BASE_URL = "$HOST/rest/V1/"

    fun url(arg: String): String {
        return BASE_URL + arg
    }

    fun urlInternal(query: String): String {
        return "https://${getEnvPrefix()}karika.ba/internal/V1/ai/suggestions?query=$query"
    }

    var token: String? = null
    fun imageUrl(name: String?): String {
        return if (name != null && name.contains("media")) {
            "$HOST/$name"
        } else {
            "$HOST/media/catalog/product$name"
        }
    }

    fun profileImage(name: String?): String? {
        if (name.isNullOrEmpty()) {
            return null
        }

        return "$HOST/media/$name"
    }

    fun chatImage(name: String): String {
        return "$HOST/media/csmessaging/chat_images/$name"
    }

    fun commentAttachment(name: String?): String {
        return "$HOST/$name"
    }

    fun orderPdf(name: String?): String {
        return "$HOST/media/order_pdf/$name"
    }

    val client: HttpClient by lazy {
        HttpClient {
            defaultRequest {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.AcceptCharset, "utf-8")
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                    explicitNulls = false
                    prettyPrint = true
                    isLenient = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }

            install("AuthHeader") {
                requestPipeline.intercept(HttpRequestPipeline.State) {
                    val token = token
                    if (!token.isNullOrBlank()) {
                        context.headers.append(HttpHeaders.Authorization, "Bearer $token")
                    }
                    proceed()
                }
            }

            install(ResponseObserver) {
                onResponse {
                    println(
                        """
                    Request: ${it.request.method.value} ${it.status} ${it.request.url}
                    ${(it.request.content as? TextContent)?.text}
                    Authorization: ${it.request.headers[Authorization]}
                    Response: ${it.bodyAsText()}
                """.trimIndent()
                    )

                }
            }
        }
    }
}