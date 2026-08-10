package karika.distribucija.ba.salesrep.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
import io.ktor.serialization.kotlinx.json.json
import karika.distribucija.ba.salesrep.BuildConfig
import kotlinx.serialization.json.Json

/**
 * Adapted from composeApp's karika.distribucija.ba.domain.HttpClientProvider - same base URL
 * scheme, auth header injection and content negotiation, but without the KMM expect/actual
 * indirection (this module is Android-only).
 */
object HttpClientProvider {
    private val HOST = "https://${PlatformEnv.envPrefix()}karika.ba/magento"
    private val BASE_URL = "$HOST/rest/V1/"

    fun url(arg: String): String = BASE_URL + arg

    fun urlV1(query: String): String = "https://${PlatformEnv.envPrefix()}karika.ba/api/V1/$query"

    var token: String? = PlatformEnv.envJwt()

    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            defaultRequest {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.AcceptCharset, "utf-8")
                header(HttpHeaders.UserAgent, PlatformEnv.userAgent())
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
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
                    val currentToken = token
                    if (!currentToken.isNullOrBlank()) {
                        context.headers.append(HttpHeaders.Authorization, "Bearer $currentToken")
                    }
                    proceed()
                }
            }

            if (BuildConfig.DEBUG) {
                install(ResponseObserver) {
                    onResponse {
                        println("SalesRep request: ${it.request.method.value} ${it.status} ${it.request.url}\nResponse: ${it.bodyAsText()}")
                    }
                }
            }
        }
    }
}
