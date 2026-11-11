package karika.distribucija.ba.salesrep.session

import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.VendorOperationsMe

/** Caches the logged-in sales rep's profile (fetched once via SalesRepository.getMe()) so any
 * screen can read employeeId/vendorId without re-fetching. */
object CurrentUser {
    var me: VendorOperationsMe? = null

    val employeeId: Long? get() = me?.employeeId
    val vendorId: Long? get() = me?.vendorId

    /** `vendor_operations_enabled` from `me`: null while unknown, false when the vendor has it off
     * or the `me` call failed - then the app shows only the "Nije dostupno" screen. */
    var vendorOpsEnabled: Boolean? = null

    /** Analytics access from `me` (`vendor_operations_enabled` + `can_see_own_analytics`): null
     * while unknown, false without the capability or when the `me` call failed. */
    var analyticsAccess: Boolean? = null

    /** Bottom of the back stack: analytics, or orders once analytics turns out not allowed. */
    val rootDestinationId: Int
        get() = if (analyticsAccess == false) R.id.ordersListFragment else R.id.analyticsOverviewFragment
}
