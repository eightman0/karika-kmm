package karika.distribucija.ba.di

import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

actual fun platformModule(): Module {
    return module {
        single<SharedPreferences> {
            androidContext().getSharedPreferences("instance_prefs", Context.MODE_PRIVATE)
        }
        single<PersistenceManager> { AndroidPersistenceManager() }
    }
}

fun initKoinAndroid(
    appDeclaration: KoinAppDeclaration = {},
) {
    initKoin(
        listOf(
            module {

            }
        ),
        appDeclaration
    )
}