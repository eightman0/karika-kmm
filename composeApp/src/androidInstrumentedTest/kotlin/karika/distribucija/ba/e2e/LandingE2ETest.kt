package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end test of the landing screen, see [StageE2ETest]. */
@OptIn(ExperimentalTestApi::class)
class LandingE2ETest : StageE2ETest() {

    @Test
    fun showsWhatKarikaIsAndHowToContinue() {
        compose.onNodeWithText(LANDING_TITLE, substring = true).assertIsDisplayed()
        compose.onNodeWithText("povezuje kupce i provjerene dobavljače", substring = true).assertIsDisplayed()
        listOf(
            "Samo za pravna lica!",
            "Direktna komunikacija kupaca i dobavljača",
            "Direktna isporuka od strane dobavljača",
            "Efikasnost i optimizacija poslovanja",
            "Prijavi se ili registruj kao:",
            "Kupac",
            "Dobavljač",
            "Nastavi kao gost",
        ).forEach { text ->
            compose.onNodeWithText(text).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun showsTheSuppliersTheBackendPromotes() {
        // The same filter LandingComponent applies to what stage.karika.ba returns
        val result = runBlocking { ProductRepository().promotedVendors().last() }
        assertTrue("promoted vendors: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        val logos = ((result as ResultState.Success<*>).data as List<PromotedVendor>)
            .filter { it.promoteVendorLogo && it.companyLogo != null }

        if (logos.isEmpty()) {
            compose.onNodeWithText("Dobavljači").assertDoesNotExist()
        } else {
            compose.onNodeWithText("Dobavljači").performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun kupacOpensTheCustomerLoginAndBackReturnsToLanding() {
        compose.onNodeWithText("Kupac").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SCREEN_TIMEOUT_MS)
        // The field's label and its placeholder
        compose.onAllNodesWithText("Email Adresa", useUnmergedTree = true).assertCountEquals(2)
        compose.onNodeWithText("Registrujte se ovdje.", substring = true).performScrollTo().assertIsDisplayed()

        pressBack()
        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Kupac").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun dobavljacOpensTheSupplierLogin() {
        compose.onNodeWithText("Dobavljač").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Prijava dobavljač"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Prijava kupac").assertDoesNotExist()
    }

    @Test
    fun nastaviKaoGostOpensTheShopAsAGuest() {
        compose.onNodeWithText("Nastavi kao gost").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Početna"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        // A guest's bottom bar: no vendors tab, a profile tab that asks to log in
        listOf("Početna", "Korpa", "Meni", "Profil").forEach { tab ->
            compose.onNodeWithText(tab).assertIsDisplayed()
        }
        compose.onNodeWithText(LANDING_TITLE, substring = true).assertDoesNotExist()
    }
}
