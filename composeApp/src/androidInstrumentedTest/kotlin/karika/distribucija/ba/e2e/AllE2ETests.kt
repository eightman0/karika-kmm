package karika.distribucija.ba.e2e

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Every end-to-end test, in the order a user goes through the app: the landing screen and the
 * logins first, then the customer's shop, then the supplier's and the sales rep's dashboards.
 * The runner would otherwise take the classes in alphabetical order. It is the default for
 * connectedStageDebugAndroidTest, see composeApp/build.gradle.kts.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LandingE2ETest::class,
    CustomerLoginE2ETest::class,
    VendorLoginE2ETest::class,
    ForgotPasswordE2ETest::class,
    // Kupac
    HomeE2ETest::class,
    CustomerBrowseE2ETest::class,
    VendorsTabE2ETest::class,
    CartE2ETest::class,
    CustomerMenuE2ETest::class,
    CustomerProfileE2ETest::class,
    // Dobavljač
    VendorNavigationE2ETest::class,
    VendorOrdersE2ETest::class,
    VendorScreensE2ETest::class,
    // Komercijalista
    SalesRepNavigationE2ETest::class,
    SalesRepScreensE2ETest::class,
)
class AllE2ETests
