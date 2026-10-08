package karika.distribucija.ba.e2e

import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.domain.model.OperationalCustomerSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.view.salesrep.dashboard.SALES_MENU_TAG
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue

/**
 * Base for the sales rep's end-to-end tests, see [DashboardE2ETest]: every test starts logged in
 * as the sales rep test account (KARIKA_STAGE_SALESREP_*, through the supplier login) on its
 * orders. Most tests only read and open screens; [SalesRepActionsE2ETest] changes the customer
 * test account's discounts, cart and orders, and cleans up after itself.
 */
abstract class SalesRepE2ETest : DashboardE2ETest() {
    override val menuTag = SALES_MENU_TAG

    override fun logInToDashboard() = logInAsSalesRep()

    /**
     * The customer test account (KARIKA_STAGE_SHOP_*) as one of the sales rep's customers. Skips
     * the calling test when it is not one, or not an active partner.
     */
    protected fun testCustomer(): OperationalCustomer {
        val email = stageAccount(Account.CUSTOMER.emailKey, Account.CUSTOMER.passwordKey).first
        val found = allCustomers().firstOrNull { it.email.equals(email, ignoreCase = true) }
        assumeTrue("the customer test account is not one of the sales rep's customers", found != null)
        assumeTrue("the customer test account is not an active partner", found!!.isActive)
        return found
    }

    /** The sales rep's customers of every status, the first 100, as the app loads them. */
    protected fun allCustomers(): List<OperationalCustomer> = asAccount(Account.SALES_REP) {
        val me = me()
        val result = runBlocking {
            if (me.capabilities.canSeeAllVendorCustomers) {
                SalesRepository().getCustomers(page = 1, pageSize = 100).last()
            } else {
                SalesRepository().getEmployeeCustomers(
                    employeeId = me.employeeId?.toInt() ?: 0, page = 1, pageSize = 100
                ).last()
            }
        }
        assertTrue("customers: $result", result is ResultState.Success)
        ((result as ResultState.Success<*>).data as OperationalCustomerSearchResults).items
    }

    /** The name the customer screens show: the company, or else the person's name. */
    protected fun OperationalCustomer.displayName() =
        company?.takeIf { it.isNotBlank() } ?: listOfNotNull(firstname, lastname).joinToString(" ")
}
