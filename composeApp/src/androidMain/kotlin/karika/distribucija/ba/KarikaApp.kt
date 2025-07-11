package karika.distribucija.ba

import android.app.Application
import karika.distribucija.ba.di.initKoinAndroid
import org.koin.android.ext.koin.androidContext

class KarikaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoinAndroid {
            androidContext(this@KarikaApp.applicationContext)
        }
    }
}