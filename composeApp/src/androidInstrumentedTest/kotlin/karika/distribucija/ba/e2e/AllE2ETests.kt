package karika.distribucija.ba.e2e

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Every end-to-end test, in the order a user goes through the app: the landing screen and the
 * logins first, then the customer's shop, then the supplier's and the sales rep's dashboards.
 * The runner would otherwise take the classes in alphabetical order. [E2ETestRunner] runs this
 * whenever all the tests are asked for.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LandingE2ETest::class,
    CustomerLoginE2ETest::class,
    VendorLoginE2ETest::class,
    ForgotPasswordE2ETest::class,
    CustomerRegistrationE2ETest::class,
    // Kupac
    HomeE2ETest::class,
    CustomerBrowseE2ETest::class,
    VendorsTabE2ETest::class,
    CartE2ETest::class,
    CustomerMenuE2ETest::class,
    CustomerProfileE2ETest::class,
    CustomerAccountE2ETest::class,
    CustomerOrderingE2ETest::class,
    CustomerMessagingE2ETest::class,
    CustomerRequestsE2ETest::class,
    CustomerPushE2ETest::class,
    // Dobavljač
    VendorNavigationE2ETest::class,
    VendorOrdersE2ETest::class,
    VendorScreensE2ETest::class,
    VendorEmployeesE2ETest::class,
    VendorOrderActionsE2ETest::class,
    VendorPushE2ETest::class,
    // Komercijalista
    SalesRepNavigationE2ETest::class,
    SalesRepScreensE2ETest::class,
    SalesRepActionsE2ETest::class,
    SalesRepMessagingE2ETest::class,
    SalesRepNewCustomerE2ETest::class,
)
class AllE2ETests
