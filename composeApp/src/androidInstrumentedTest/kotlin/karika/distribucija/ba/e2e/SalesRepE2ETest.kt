package karika.distribucija.ba.e2e

import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import karika.distribucija.ba.ui.view.salesrep.dashboard.SALES_MENU_TAG
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue

/**
 * Base for the sales rep's end-to-end tests, see [DashboardE2ETest]: every test starts logged in
 * as the sales rep test account (KARIKA_STAGE_SALESREP_*, through the supplier login) on its
 * orders. The tests only read and open screens; nothing is ordered, sent, saved or deleted, and
 * no customer's cart is touched.
 */
abstract class SalesRepE2ETest : DashboardE2ETest() {
    override val menuTag = SALES_MENU_TAG

    override fun logInToDashboard() = logInAsSalesRep()

    /** Who the logged-in sales rep is and what they may do, as the app reads it. */
    protected fun me(): VendorOperationsMe {
        val result = runBlocking { SalesRepository().getMe().last() }
        assertTrue("sales rep: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorOperationsMe
    }
}
