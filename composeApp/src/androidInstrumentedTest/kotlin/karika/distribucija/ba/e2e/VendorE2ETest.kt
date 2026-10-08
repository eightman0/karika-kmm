package karika.distribucija.ba.e2e

import karika.distribucija.ba.ui.components.DASHBOARD_MENU_TAG

/**
 * Base for the supplier's end-to-end tests, see [DashboardE2ETest]: every test starts logged in
 * as the supplier test account (KARIKA_STAGE_DISTRIBUTER_*) on its analytics overview. Most tests
 * only read and open screens; [VendorOrderActionsE2ETest] works on orders it makes for the purpose.
 */
abstract class VendorE2ETest : DashboardE2ETest() {
    override val menuTag = DASHBOARD_MENU_TAG

    override fun logInToDashboard() = logInAsVendor()
}
