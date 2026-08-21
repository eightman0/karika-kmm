package karika.distribucija.ba.salesrep.session

import android.content.Context
import androidx.core.content.edit
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.network.HttpClientProvider
import karika.distribucija.ba.salesrep.network.PlatformEnv

/**
 * Adapted from composeApp's SessionHandler.kt, trimmed to what a standalone sales-rep-only
 * app needs: no multi-role branching, since this app IS the sales-rep flow.
 */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("salesrep_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_JWT, token) }
        HttpClientProvider.token = token
    }

    fun restoreTokenIfPresent() {
        val token = prefs.getString(KEY_JWT, null)
        if (!token.isNullOrBlank()) {
            HttpClientProvider.token = token
        }
    }

    fun hasToken(): Boolean {
        val token = prefs.getString(KEY_JWT, null)
        return !token.isNullOrBlank() && token != PlatformEnv.envJwt()
    }

    fun logout() {
        prefs.edit { remove(KEY_JWT) }
        HttpClientProvider.token = PlatformEnv.envJwt()
        AppLogger.setUser(null)
    }

    fun rememberedEmail(): String = prefs.getString(KEY_REMEMBERED_EMAIL, "").orEmpty()

    fun rememberedPassword(): String = prefs.getString(KEY_REMEMBERED_PASSWORD, "").orEmpty()

    fun saveRememberedCredentials(email: String, password: String) {
        prefs.edit {
            putString(KEY_REMEMBERED_EMAIL, email)
            putString(KEY_REMEMBERED_PASSWORD, password)
        }
    }

    fun clearRememberedCredentials() {
        prefs.edit {
            remove(KEY_REMEMBERED_EMAIL)
            remove(KEY_REMEMBERED_PASSWORD)
        }
    }

    companion object {
        private const val KEY_JWT = "jwt_token"
        private const val KEY_REMEMBERED_EMAIL = "remembered_email"
        private const val KEY_REMEMBERED_PASSWORD = "remembered_password"
    }
}
