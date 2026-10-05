package karika.distribucija.ba.e2e

import karika.distribucija.ba.ui.components.DASHBOARD_MENU_TAG

/**
 * Base for the supplier's end-to-end tests, see [DashboardE2ETest]: every test starts logged in
 * as the supplier test account (KARIKA_STAGE_DISTRIBUTER_*) on its analytics overview. The tests
 * only read and open screens; nothing is approved, sent, saved or deleted.
 */
abstract class VendorE2ETest : DashboardE2ETest() {
    override val menuTag = DASHBOARD_MENU_TAG

    override fun logInToDashboard() = logInAsVendor()
}
