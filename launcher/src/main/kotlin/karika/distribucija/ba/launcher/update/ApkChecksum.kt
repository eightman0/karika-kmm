package karika.distribucija.ba.launcher.update

import java.io.File
import java.security.MessageDigest

/** Shared by UpdateWorker (salesrep) and LauncherSelfUpdateWorker (launcher itself) - both verify
 * a downloaded APK's sha256 against what the dashboard published before installing it. */
object ApkChecksum {
    private const val DIGEST_BUFFER_SIZE = 8192

    /** True if expectedSha256 is blank (nothing published to check against - not our call to
     * block on) or the file's actual sha256 matches it. */
    fun verifySha256(file: File, expectedSha256: String): Boolean {
        if (expectedSha256.isBlank()) return true
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DIGEST_BUFFER_SIZE)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        return actual.equals(expectedSha256, ignoreCase = true)
    }
}
