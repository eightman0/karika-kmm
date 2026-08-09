package karika.distribucija.ba.salesrep.session

import karika.distribucija.ba.salesrep.model.VendorOperationsMe

/** Caches the logged-in sales rep's profile (fetched once via SalesRepository.getMe()) so any
 * screen can read employeeId/vendorId without re-fetching. */
object CurrentUser {
    var me: VendorOperationsMe? = null

    val employeeId: Long? get() = me?.employeeId
    val vendorId: Long? get() = me?.vendorId
}
