package karika.distribucija.ba.util

import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.api.MagicLinkRepository
import karika.distribucija.ba.domain.model.MagicLinkDestination
import karika.distribucija.ba.domain.model.MagicLinkResult
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.salesrep.dashboard.SalesRepConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Magic links from email/Viber: https://<host>/magic-links/<64 hex token>.
 * The token is resolved against Laravel (magic-links/resolve) with the current session and
 * the user is taken to the order screen. If nobody is logged in, the token waits until login.
 * Navigation mirrors PushHandler's orderStatusChange handling.
 */
object MagicLinkHandler {
    private val pathRegex = Regex("""^/magic-links/([^/]*)/?$""")
    private val tokenRegex = Regex("""^[a-fA-F0-9]{64}$""")

    private const val ERROR_TITLE = "Link nije moguće otvoriti"

    private var pendingToken: String? = null

    /** Returns true if [path] is a magic link (handled now, kept until login, or rejected). */
    fun handlePath(path: String?, component: CommonComponent): Boolean {
        val token = path?.let { pathRegex.find(it)?.groupValues?.get(1) } ?: return false
        if (!tokenRegex.matches(token)) {
            showInfo(component, ERROR_TITLE, "Link nije ispravan.")
            return true
        }
        pendingToken = token
        if (!isLoggedIn(component)) {
            showLoginRequired(component)
            return true
        }
        resolvePendingIfLoggedIn(component)
        return true
    }

    /**
     * Called whenever the app stack changes, so a token kept before login resolves after it.
     * Stays silent while nobody is logged in (also as a guest): the user was already told once,
     * when the link arrived, and every screen change must not show the dialog again.
     */
    fun resolvePendingIfLoggedIn(component: CommonComponent) {
        if (!isLoggedIn(component)) {
            return
        }
        val token = pendingToken ?: return
        pendingToken = null

        CoroutineScope(Dispatchers.Main).launch {
            component.showLoader()
            val result = MagicLinkRepository().resolve(token)
            component.hideLoader()
            when (result) {
                is MagicLinkResult.Success -> navigate(result.destination, component)
                is MagicLinkResult.Unauthenticated -> {
                    // Session is no longer valid: log in again, then retry the same token.
                    pendingToken = token
                    component.logout()
                    showLoginRequired(component)
                }

                is MagicLinkResult.Error -> showInfo(component, ERROR_TITLE, result.message)
            }
        }
    }

    /** A guest has the app's own JWT, for which mainConfig() is PreLogin as well. */
    private fun isLoggedIn(component: CommonComponent): Boolean =
        component.stateHolder.sessionHandler.mainConfig() !is AppConfig.PreLogin

    private fun showLoginRequired(component: CommonComponent) {
        showInfo(component, "Potrebna prijava", "Prijavite se da biste otvorili link.")
    }

    private fun showInfo(component: CommonComponent, title: String, message: String) {
        component.stateHolder.commonHandler.magicLinkInfo.value = title to message
    }

    private fun navigate(destination: MagicLinkDestination, component: CommonComponent) {
        val orderId = destination.orderId
        when (destination.type) {
            "customer_order_approved_email",
            "customer_order_reject_email",
            "customer_order_viber" -> component.appNavigate(
                AppConfig.OrderDetails(OrdersResponse(orderId = orderId, incrementId = orderId))
            )

            "vendor_order_viber" -> when (component.stateHolder.sessionHandler.mainConfig()) {
                AppConfig.SalesRep -> component.salesRepNavigate(
                    SalesRepConfig.OrderDetail(OnBehalfOrder(incrementId = orderId))
                )

                else -> component.dashNavigate(
                    DashConfig.OrderDetails(VendorOrder(orderId = orderId))
                )
            }

            else -> showInfo(component, ERROR_TITLE, "Link nije podržan u ovoj verziji aplikacije.")
        }
    }
}
