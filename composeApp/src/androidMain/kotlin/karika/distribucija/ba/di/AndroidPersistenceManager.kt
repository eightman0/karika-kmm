package karika.distribucija.ba.di

import android.content.SharedPreferences
import androidx.core.content.edit
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


class AndroidPersistenceManager : PersistenceManager, KoinComponent {
    private val preferences: SharedPreferences = get()

    override fun save(key: String, value: String) {
        preferences.edit {
            putString(key, value)
        }
    }

    override fun get(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    override fun clear() {
        preferences.edit {
            clear()
        }
    }
}