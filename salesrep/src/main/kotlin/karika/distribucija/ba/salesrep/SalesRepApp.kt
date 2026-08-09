package karika.distribucija.ba.salesrep

import android.app.Application
import karika.distribucija.ba.salesrep.session.SessionManager

class SalesRepApp : Application() {
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        sessionManager.restoreTokenIfPresent()
    }
}
