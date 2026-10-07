package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.urlV1
import karika.distribucija.ba.domain.model.MagicLinkErrorResponse
import karika.distribucija.ba.domain.model.MagicLinkResolveRequest
import karika.distribucija.ba.domain.model.MagicLinkResolveResponse
import karika.distribucija.ba.domain.model.MagicLinkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class MagicLinkApi {
    suspend fun resolve(token: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            urlV1("magic-links/resolve")
        ) {
            setBody(MagicLinkResolveRequest(token))
        }
    }
}

class MagicLinkRepository internal constructor() {
    suspend fun resolve(token: String): MagicLinkResult = withContext(Dispatchers.Default) {
        try {
            val response = MagicLinkApi()
                .resolve(token)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                return@withContext MagicLinkResult.Success(
                    response.body<MagicLinkResolveResponse>().data
                )
            }

            val code = runCatching { response.body<MagicLinkErrorResponse>().error?.code }
                .getOrNull()
            // Server contract: branch on error.code, error.message is English display text.
            when (code) {
                "unauthenticated" -> MagicLinkResult.Unauthenticated
                "forbidden" -> MagicLinkResult.Error("Ovaj link nije namijenjen Vašem nalogu.")
                "invalid_token" -> MagicLinkResult.Error("Link nije ispravan.")
                "expired" -> MagicLinkResult.Error("Link je istekao.")
                "used" -> MagicLinkResult.Error("Link je već iskorišten.")
                "destination_unresolved" -> MagicLinkResult.Error("Link nema odredište.")
                "rate_limit_exceeded" -> MagicLinkResult.Error("Previše zahtjeva. Pokušajte ponovo za minutu.")
                else -> if (response.status == HttpStatusCode.Unauthorized) {
                    MagicLinkResult.Unauthenticated
                } else {
                    MagicLinkResult.Error("Došlo je do greške. Pokušajte ponovo!")
                }
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            MagicLinkResult.Error(e.message ?: "Došlo je do greške. Pokušajte ponovo!")
        }
    }
}
