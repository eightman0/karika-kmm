package karika.distribucija.ba.launcher.update

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Backs the silent-update mechanism for the payload app (salesrep): the admin dashboard writes
 * the published version to a single Firestore doc, and this does a plain one-shot read of it -
 * no live listener. That's deliberate: a listener needs the process to stay unfrozen to receive
 * pushes, which is what KioskMessagingService (an FCM data message on publish) and the periodic
 * UpdateWorker run (both of which wake the process themselves) are for instead.
 */
object VersionConfigProvider {
    private const val COLLECTION = "config"
    private const val DOCUMENT = "kiosk_version"
    private const val TIMEOUT_MS = 15_000L

    // Firestore's client has no deadline of its own for a stuck connection - it just keeps
    // retrying its stream underneath - so without this a bad network moment hangs the whole
    // UpdateWorker run (and eats into WorkManager's execution budget) instead of failing fast into
    // Result.retry() like every other transient error here already does.
    suspend fun fetchLatest(): KioskVersion = withTimeout(TIMEOUT_MS) {
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously().await()
        }
        val snapshot = Firebase.firestore.collection(COLLECTION).document(DOCUMENT).get().await()
        KioskVersion(
            versionCode = snapshot.getLong("versionCode") ?: 0L,
            versionName = snapshot.getString("versionName").orEmpty(),
            apkUrl = snapshot.getString("apkUrl").orEmpty(),
            apkSha256 = snapshot.getString("apkSha256").orEmpty(),
            mandatory = snapshot.getBoolean("mandatory") ?: true
        )
    }
}
