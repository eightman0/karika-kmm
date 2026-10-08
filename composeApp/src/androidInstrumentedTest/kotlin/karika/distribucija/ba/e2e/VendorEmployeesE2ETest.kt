package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.EmployeesRepository
import karika.distribucija.ba.domain.model.EmployeeLocationHistory
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorEmployee
import karika.distribucija.ba.domain.model.VendorEmployeeSearchResults
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * End-to-end test of the supplier's "Komercijalisti" on stage.karika.ba, see [VendorE2ETest]:
 * the drawer item, the team list against stage (status filter, search) and an employee's
 * "Historija lokacija" with its periods. Read only.
 */
@OptIn(ExperimentalTestApi::class)
class VendorEmployeesE2ETest : VendorE2ETest() {

    @Test
    fun drawerShowsKomercijalistiExactlyWhenTheSupplierMaySeeTheTeam() {
        val canView = canViewEmployees()

        openDrawer()

        assertEquals(canView, displayed(drawerItem("Komercijalisti")))
    }

    @Test
    fun komercijalistiListsStagesTeam() {
        assumeTrue("the supplier may not see the team", canViewEmployees())
        val team = employees()
        openEmployees()

        compose.waitUntilAtLeastOneExists(hasText("Ukupno: ${team.totalCount}"), SERVER_TIMEOUT_MS)
        if (team.items.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema komercijalista"), SERVER_TIMEOUT_MS)
            return
        }
        team.items.forEach { scrollListTo(hasText(it.fullName)) }
    }

    @Test
    fun statusPillsFilterTheTeam() {
        assumeTrue("the supplier may not see the team", canViewEmployees())
        openEmployees()
        listOf("Svi", "Aktivni", "Pozvani", "Suspendovani").forEach {
            assertTrue("\"$it\" is not offered", exists(hasText(it)))
        }

        compose.onNodeWithText("Aktivni").performClick()

        val active = employees(status = "active")
        compose.waitUntilAtLeastOneExists(hasText("Ukupno: ${active.totalCount}"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        if (active.items.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema komercijalista"), SERVER_TIMEOUT_MS)
        } else {
            scrollListTo(hasText(active.items.first().fullName))
        }
    }

    @Test
    fun searchFindsAnEmployeeByEmail() {
        assumeTrue("the supplier may not see the team", canViewEmployees())
        val employee = employees().items.firstOrNull { !it.email.isNullOrBlank() }
        assumeTrue("no employee with an email", employee != null)
        val found = employees(search = employee!!.email)
        assertTrue("stage does not find ${employee.email}", found.items.any { it.employeeId == employee.employeeId })
        openEmployees()

        compose.onNode(hasSetTextAction()).performTextInput(employee.email!!)
        compose.onNode(hasSetTextAction()).performImeAction()
        closeKeyboard()

        compose.waitUntilAtLeastOneExists(hasText("Ukupno: ${found.totalCount}"), SERVER_TIMEOUT_MS)
        scrollListTo(hasText(employee.fullName))
    }

    @Test
    fun historijaLokacijaShowsTheEmployeesLocationsForThePeriod() {
        assumeTrue("the supplier may not see the team", canViewEmployees())
        val team = employees().items.filter { it.canOpenLocations }
        assumeTrue("no employee whose locations the supplier may see", team.isNotEmpty())
        openEmployees()
        scrollListTo(hasText("Historija lokacija"))

        compose.onAllNodesWithText("Historija lokacija").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Zadnjih 30 dana"), SCREEN_TIMEOUT_MS)
        val employee = team.firstOrNull { exists(hasText(it.fullName)) }
        assertTrue("the screen shows none of ${team.map { it.fullName }}", employee != null)
        listOf("Danas", "Jučer", "Zadnjih 7 dana").forEach {
            assertTrue("\"$it\" is not offered", exists(hasText(it)))
        }
        assertShows(locations(employee!!, daysBack = 0), "Danas")

        compose.onNodeWithText("Zadnjih 30 dana").performClick()
        assertShows(locations(employee, daysBack = 29), "Zadnjih 30 dana")
    }

    @Test
    fun theBackLinkOnTheLocationsReturnsToTheTeam() {
        assumeTrue("the supplier may not see the team", canViewEmployees())
        val employee = employees().items.firstOrNull { it.canOpenLocations }
        assumeTrue("no employee whose locations the supplier may see", employee != null)
        openEmployees()
        scrollListTo(hasText("Historija lokacija"))
        compose.onAllNodesWithText("Historija lokacija").onFirst().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Zadnjih 30 dana"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        // The back link, not the drawer's item of the same name, which is off screen
        val links = compose.onAllNodesWithText("Komercijalisti")
        val link = (0 until links.fetchSemanticsNodes().size).first {
            runCatching { links[it].assertIsDisplayed() }.isSuccess
        }
        links[link].performClick()

        compose.waitUntilAtLeastOneExists(hasText("Pretraži po imenu ili emailu"), SCREEN_TIMEOUT_MS)
        compose.waitUntilDoesNotExist(hasText("Zadnjih 30 dana"), SCREEN_TIMEOUT_MS)
    }

    /** What the locations screen shows for [history], the period [period] being selected. */
    private fun assertShows(history: EmployeeLocationHistory, period: String) {
        when {
            !history.hasAny ->
                compose.waitUntilAtLeastOneExists(hasText("Bez lokacija"), SERVER_TIMEOUT_MS)
            history.totalCount == 0 || history.items.isEmpty() ->
                compose.waitUntilAtLeastOneExists(hasText("Nema lokacija u periodu"), SERVER_TIMEOUT_MS)
            else -> {
                compose.waitUntilAtLeastOneExists(hasText("Tačaka"), SERVER_TIMEOUT_MS)
                assertTrue(
                    "$period: ${history.totalCount} points are not shown",
                    exists(hasText(history.totalCount.toString()), unmerged = true)
                )
                scrollListTo(hasText("Kretanje"))
            }
        }
        waitUntilLoaded()
    }

    private fun openEmployees() {
        goTo("Komercijalisti", title = "Pretraži po imenu ili emailu")
    }

    private fun canViewEmployees() = me().let { it.vendorOperationsEnabled && it.capabilities.canViewEmployees }

    /** The team's first page, the way the "Komercijalisti" screen loads it. */
    private fun employees(search: String? = null, status: String? = null): VendorEmployeeSearchResults {
        val result = runBlocking {
            EmployeesRepository().getEmployees(page = 1, pageSize = 12, search = search, status = status).last()
        }
        assertTrue("employees: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorEmployeeSearchResults
    }

    /** The employee's locations from [daysBack] days ago up to today, as the screen asks for them. */
    @OptIn(ExperimentalTime::class)
    private fun locations(employee: VendorEmployee, daysBack: Int): EmployeeLocationHistory {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val result = runBlocking {
            EmployeesRepository().getLocations(
                employee.employeeId!!,
                today.minus(daysBack, DateTimeUnit.DAY).toString(),
                today.toString()
            ).last()
        }
        assertTrue("locations: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as EmployeeLocationHistory
    }
}
