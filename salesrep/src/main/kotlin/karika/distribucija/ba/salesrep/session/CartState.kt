package karika.distribucija.ba.salesrep.session

import androidx.lifecycle.MutableLiveData
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponse

/**
 * Server-backed on-behalf cart, shared across catalog/cart screens for whichever customer is
 * currently being ordered for - mirrors composeApp's SalesSpecificHandler.cart. Always refreshed
 * from the server when a catalog screen for a customer is opened, so switching customers can't
 * leak a stale cart.
 */
object CartState {
    val cart = MutableLiveData<OnBehalfCartResponse?>(null)

    fun clear() {
        cart.value = null
    }
}
