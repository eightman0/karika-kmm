package karika.distribucija.ba.di

interface PersistenceManager {
    fun save(key: String, value: String)
    fun get(key: String): String
    fun clear()
}