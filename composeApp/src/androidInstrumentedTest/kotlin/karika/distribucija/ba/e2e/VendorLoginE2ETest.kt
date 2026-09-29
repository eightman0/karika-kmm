package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the supplier login on stage.karika.ba, see [StageE2ETest]. The account
 * comes from KARIKA_STAGE_DISTRIBUTER_EMAIL and KARIKA_STAGE_DISTRIBUTER_PASSWORD, see
 * [stageAccount].
 */
@OptIn(ExperimentalTestApi::class)
class VendorLoginE2ETest : StageE2ETest() {

    private lateinit var email: String
    private lateinit var password: String

    @Before
    fun openVendorLogin() {
        val (email, password) = stageAccount("KARIKA_STAGE_DISTRIBUTER_EMAIL", "KARIKA_STAGE_DISTRIBUTER_PASSWORD")
        this.email = email
        this.password = password

        compose.onNodeWithText("Dobavljač").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava dobavljač"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun wrongPasswordShowsAnErrorAndStaysOnLogin() {
        logIn(email, password + "-pogresna")

        compose.waitUntilAtLeastOneExists(hasText(WRONG_LOGIN_MESSAGE, substring = true), SERVER_TIMEOUT_MS)
        compose.onNodeWithText("Prijava dobavljač").assertIsDisplayed()
        compose.onNodeWithText(DASHBOARD_TITLE, substring = true).assertDoesNotExist()
    }

    @Test
    fun correctPasswordOpensTheDashboard() {
        logIn(email, password)

        // A supplier starts on the analytics overview, with the dashboard menu in the drawer
        compose.waitUntilAtLeastOneExists(hasText(DASHBOARD_TITLE, substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        compose.onNodeWithText(DASHBOARD_TITLE, substring = true).assertIsDisplayed()
        listOf("Upravljanje narudžbama", "Upravljanje rabatima", "Poruke kupaca", "Odjavi se").forEach { item ->
            compose.onNodeWithText(item).assertExists()
        }
        compose.onNodeWithText("Prijava dobavljač").assertDoesNotExist()
        compose.onNodeWithText(WRONG_LOGIN_MESSAGE, substring = true).assertDoesNotExist()
    }

    private companion object {
        const val DASHBOARD_TITLE = "Analitika — Pregled"
    }
}
