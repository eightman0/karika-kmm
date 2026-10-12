package karika.distribucija.ba.e2e

import android.location.Location
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.location.LocationServices
import karika.distribucija.ba.domain.api.DeviceIdentifier
import karika.distribucija.ba.domain.api.EmployeesRepository
import karika.distribucija.ba.domain.model.EmployeeLocationHistory
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.components.DASHBOARD_MENU_TAG
import karika.distribucija.ba.ui.view.salesrep.dashboard.SALES_MENU_TAG
import karika.distribucija.ba.util.serverTimeInSarajevo
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * End-to-end test of the sales rep's location on stage.karika.ba, see [SalesRepE2ETest]: right
 * after every login the app sends the device's location (the hourly sends come later). The test
 * logs out and in twice and after each login waits for a new location from this device in the
 * sales rep's location history, read through the API as the supplier. Then it logs in as the
 * supplier and finds the last of them in "Komercijalisti" → "Historija lokacija".
 *
 * The emulator has no network location, and the app asks for one at balanced power, which does
 * not use GPS, so the test gives the fused provider a mock location of its own before each login,
 * different every time, and checks that stage got exactly that one.
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepLocationE2ETest : SalesRepE2ETest() {

    /** The test ends on the supplier's dashboard, whose drawer opens from another icon. */
    private var onVendorDashboard = false

    override val startOnOrders = false

    override val menuTag get() = if (onVendorDashboard) DASHBOARD_MENU_TAG else SALES_MENU_TAG

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(targetContext()) }

    @After
    fun endMockLocation() {
        runCatching { runBlocking { fused.setMockMode(false).await() } }
    }

    @Test
    fun everyLoginSendsTheLocation() {
        val employeeId = me().employeeId
        assumeTrue("the sales rep has no employee id", employeeId != null)
        val device = DeviceIdentifier.deviceId()
        val email = stageAccount(Account.SALES_REP.emailKey, Account.SALES_REP.passwordKey).first
        var last: EmployeeLocationPoint? = null

        repeat(LOGINS) { login ->
            val before = latestLocationId(employeeId!!)
            // Somewhere in Sarajevo, a different spot for every login
            val here = mockLocation(SARAJEVO_LAT + Math.random() / 100, SARAJEVO_LON + Math.random() / 100)
            logOut()
            logInAgain()

            var sent: EmployeeLocationPoint? = null
            runCatching {
                compose.waitUntil(LOCATION_TIMEOUT_MS) {
                    Thread.sleep(POLL_MS)
                    sent = locations(employeeId).items.firstOrNull { it.locationId > before && it.deviceId == device }
                    sent != null
                }
            }
            assertTrue(
                "login ${login + 1}: no new location from this device ($device) on stage within " +
                    "${LOCATION_TIMEOUT_MS / 1000} s, the latest before the login was $before",
                sent != null
            )
            assertEquals("login ${login + 1}: latitude", here.latitude, sent!!.latitude, COORDINATE_DELTA)
            assertEquals("login ${login + 1}: longitude", here.longitude, sent!!.longitude, COORDINATE_DELTA)
            last = sent
        }

        assertShownToTheSupplier(email, last!!)
    }

    /** "Komercijalisti" → the sales rep → "Historija lokacija" shows [point] for "Danas". */
    private fun assertShownToTheSupplier(email: String, point: EmployeeLocationPoint) {
        // Without a saved supplier session the supplier logs in from the landing screen
        logOut()
        logInAsVendor()
        onVendorDashboard = true
        goTo("Komercijalisti", title = "Pretraži po imenu ili emailu")
        compose.onNode(hasSetTextAction()).performTextInput(email)
        compose.onNode(hasSetTextAction()).performImeAction()
        closeKeyboard()
        scrollListTo(hasText("Historija lokacija"))
        compose.onNodeWithText("Historija lokacija").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Zadnjih 30 dana"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        // The timeline row: the time in Sarajevo and the coordinates, as the screen writes them
        val time = point.effectiveAt.serverTimeInSarajevo()
            ?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
            ?: point.effectiveAt
        val coordinates = "${point.latitude.format5()}, ${point.longitude.format5()}"
        // The row's texts are separate nodes; the coordinates are this test's own
        val found = runCatching { scrollListTo(hasText(coordinates)) }.isSuccess && exists(hasText(time))
        if (!found) dumpScreen("the supplier's location history")
        assertTrue("the supplier's \"Historija lokacija\" does not show $time $coordinates", found)
    }

    private fun logOut() {
        openDrawer()
        compose.onNode(drawerItem("Odjavi se")).performClick()
        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /** Logs in through the screen, as the sales rep does every time, not with a saved session. */
    private fun logInAgain() {
        val (email, password) = stageAccount(Account.SALES_REP.emailKey, Account.SALES_REP.passwordKey)
        compose.onNodeWithText("Dobavljač").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava dobavljač"), SCREEN_TIMEOUT_MS)
        logIn(email, password)
        waitAfterLogIn(hasText("Komercijalista"))
        waitUntilLoaded()
    }

    /** Makes [latitude], [longitude] the device's location for the app, through the fused provider's mock mode. */
    private fun mockLocation(latitude: Double, longitude: Double): Location {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val grant = instrumentation.uiAutomation.executeShellCommand(
            "appops set ${targetContext().packageName} android:mock_location allow"
        )
        ParcelFileDescriptor.AutoCloseInputStream(grant).use { it.readBytes() }
        val location = Location("fused").apply {
            this.latitude = latitude
            this.longitude = longitude
            accuracy = MOCK_ACCURACY_M
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        runBlocking {
            fused.setMockMode(true).await()
            fused.setMockLocation(location).await()
        }
        return location
    }

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun Double.format5() = String.format(java.util.Locale.US, "%.5f", this)

    private fun latestLocationId(employeeId: Long) = locations(employeeId).items.maxOfOrNull { it.locationId } ?: 0L

    /** The sales rep's locations from yesterday to tomorrow, so that no time zone cuts today off. */
    @OptIn(ExperimentalTime::class)
    private fun locations(employeeId: Long): EmployeeLocationHistory = asAccount(Account.VENDOR) {
        val today = Clock.System.todayIn(TimeZone.UTC)
        val result = runBlocking {
            EmployeesRepository().getLocations(
                employeeId,
                today.minus(1, DateTimeUnit.DAY).toString(),
                today.plus(1, DateTimeUnit.DAY).toString()
            ).last()
        }
        assertTrue("the supplier could not read the sales rep's locations: $result", result is ResultState.Success)
        (result as ResultState.Success<*>).data as EmployeeLocationHistory
    }

    private companion object {
        const val LOGINS = 2
        const val LOCATION_TIMEOUT_MS = 60_000L
        const val POLL_MS = 2_000L
        const val SARAJEVO_LAT = 43.85
        const val SARAJEVO_LON = 18.41
        const val MOCK_ACCURACY_M = 5f
        /** Stage keeps more decimals than the screen shows; this is well under a meter. */
        const val COORDINATE_DELTA = 0.000_01
    }
}
