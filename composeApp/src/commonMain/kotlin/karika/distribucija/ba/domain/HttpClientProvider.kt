package karika.distribucija.ba.domain

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientProvider {
    var token: String? = null
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
                    Authorization: ${it.request.headers[Authorization]}
                    Response: ${it.bodyAsText()}
                """.trimIndent()
                    )

                }
            }
        }
    }
}