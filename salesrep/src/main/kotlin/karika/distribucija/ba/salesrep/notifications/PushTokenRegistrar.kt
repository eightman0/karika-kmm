package karika.distribucija.ba.salesrep.notifications

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import karika.distribucija.ba.salesrep.api.SalesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Registers/clears this device's FCM token with the backend - mirrors composeApp's
 * CommonComponent.savePushHandle()/removePushHandle(). Best-effort: a failure here shouldn't
 * block login/logout, so errors are silently swallowed. */
object PushTokenRegistrar {

    fun register() {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val tokenId = FirebaseInstallations.getInstance().id.await()
                val token = FirebaseMessaging.getInstance().token.await()
                SalesRepository().savePushToken(token, tokenId).collect { }
            }
        }
    }

    fun unregister() {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val tokenId = FirebaseInstallations.getInstance().id.await()
                SalesRepository().savePushToken(null, tokenId).collect { }
            }
        }
    }
}
