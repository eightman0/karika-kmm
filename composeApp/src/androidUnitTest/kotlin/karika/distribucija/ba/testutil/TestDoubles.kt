package karika.distribucija.ba.testutil

import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.ui.common.KarikaHandler

class InMemoryPersistenceManager : PersistenceManager {
    private val values = mutableMapOf<String, String>()

    override fun save(key: String, value: String) {
        values[key] = value
    }

    override fun get(key: String): String = values[key] ?: ""

    override fun clear() {
        values.clear()
    }
}

/** A platform handler without a platform; [photo] is what "picking" a photo returns, if set. */
class FakeKarikaHandler(var photo: Pair<String, ByteArray>? = null) : KarikaHandler {
    override fun pickFile(mediaTypes: Array<String>, callback: (String, ByteArray) -> Unit) {}

    override fun pickPhoto(callback: (String, ByteArray) -> Unit) {
        photo?.let { (name, data) -> callback(name, data) }
    }

    override fun downloadFile(fileName: String, fileType: String, fileUrl: String) {}

    override fun getPushHandle(callback: (String, String) -> Unit) {}
}
