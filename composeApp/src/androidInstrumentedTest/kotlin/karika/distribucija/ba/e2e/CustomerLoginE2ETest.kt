package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer login on stage.karika.ba, see [StageE2ETest]. The account
 * comes from KARIKA_STAGE_SHOP_EMAIL and KARIKA_STAGE_SHOP_PASSWORD, see [stageAccount].
 */
@OptIn(ExperimentalTestApi::class)
class CustomerLoginE2ETest : StageE2ETest() {

    private lateinit var email: String
    private lateinit var password: String

    @Before
    fun openCustomerLogin() {
        val (email, password) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")
        this.email = email
        this.password = password

        compose.onNodeWithText("Kupac").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun wrongPasswordShowsAnErrorAndStaysOnLogin() {
        logIn(email, password + "-pogresna")

        compose.waitUntilAtLeastOneExists(hasText(WRONG_LOGIN_MESSAGE, substring = true), SERVER_TIMEOUT_MS)
        compose.onNodeWithText("Prijava kupac").assertIsDisplayed()
        compose.onNodeWithText("Početna").assertDoesNotExist()
    }

    @Test
    fun correctPasswordOpensTheShop() {
        logIn(email, password)

        compose.waitUntilAtLeastOneExists(hasText("Početna"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        // A logged-in customer's bottom bar has the vendors tab a guest does not
        // (home can show "Dobavljači" as a section too, so only that each one is there)
        listOf("Početna", "Dobavljači", "Meni", "Korpa", "Profil").forEach { tab ->
            compose.onAllNodesWithText(tab).assertCountAtLeastOne(tab)
        }
        compose.onNodeWithText("Prijava kupac").assertDoesNotExist()
        compose.onNodeWithText(WRONG_LOGIN_MESSAGE, substring = true).assertDoesNotExist()
    }

    private fun SemanticsNodeInteractionCollection.assertCountAtLeastOne(text: String) {
        assertTrue("\"$text\" is not shown", fetchSemanticsNodes().isNotEmpty())
    }
}
