package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.DASHBOARD_NOTIFICATIONS_TAG
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end test of the supplier dashboard's frame on stage.karika.ba, see [VendorE2ETest]:
 * the first screen, the drawer and where each of its items goes, notifications and logout.
 */
@OptIn(ExperimentalTestApi::class)
class VendorNavigationE2ETest : VendorE2ETest() {

    @Test
    fun startsOnTheAnalyticsOverview() {
        compose.onNodeWithText(VENDOR_HOME).assertIsDisplayed()
        compose.onNodeWithText("Ukupan prihod:").assertIsDisplayed()
        compose.onNodeWithText("Ukupan broj narudžbi:").assertIsDisplayed()
    }

    @Test
    fun drawerShowsTheSupplierAndEveryItem() {
        val vendor = profile()

        openDrawer()

        vendor.publicName?.let { assertTrue("\"$it\" is not shown", displayed(hasText(it))) }
        vendor.email?.let { assertTrue("\"$it\" is not shown", displayed(hasText(it))) }
        listOf(
            "Analitika", "Narudžbe", "Rabati", "Poruke kupaca",
            "Poruke admina", "Interne poruke", "Korisnički profil", "Odjavi se"
        ).forEach { assertTrue("\"$it\" is not in the drawer", displayed(drawerItem(it))) }
        // Product management is switched off in the app
        assertTrue(!exists(hasText("Upravljanje artiklima")))
    }

    @Test
    fun analitikaOpensItsSubItems() {
        openDrawer()
        compose.onNode(drawerItem("Analitika")).performClick()

        listOf(
            "Pregled", "Trendovi prodaje", "Analitika kupaca", "Kupci koji zahtijevaju pažnju",
            "Proizvodi i kategorije"
        ).forEach { compose.waitUntil(SCREEN_TIMEOUT_MS) { displayed(drawerItem(it)) } }
    }

    @Test
    fun trendoviProdajeOpensTheTrends() {
        goTo("Trendovi prodaje", "Analitika — Trendovi prodaje", inAnalytics = true)
        compose.onNodeWithText("Prihod").assertExists()
    }

    @Test
    fun analitikaKupacaOpensTheCustomerAnalytics() {
        goTo("Analitika kupaca", "Analitika — Analitika kupaca", inAnalytics = true)
        compose.onNodeWithText("Dodijeljeni kupci").assertExists()
    }

    @Test
    fun kupciKojiZahtijevajuPaznjuOpensTheAtRiskCustomers() {
        goTo("Kupci koji zahtijevaju pažnju", inAnalytics = true)
        compose.onNodeWithText("Neaktivnost se mjeri od danas, ne od odabranog perioda.").assertIsDisplayed()
    }

    @Test
    fun proizvodiIKategorijeOpensTheProductAnalytics() {
        goTo("Proizvodi i kategorije", inAnalytics = true)
        compose.onNodeWithText("Proizvodi").assertIsDisplayed()
        compose.onNodeWithText("Kategorije").assertIsDisplayed()
    }

    @Test
    fun upravljanjeNarudzbamaOpensTheOrders() {
        goTo("Narudžbe", "Minimalna vrijednost narudžbe")
        compose.onNodeWithText("Minimalna vrijednost narudžbe").assertIsDisplayed()
    }

    @Test
    fun upravljanjeRabatimaOpensTheDiscountRules() {
        goTo("Rabati")
        compose.onNodeWithText("Po kupcu").assertIsDisplayed()
    }

    @Test
    fun porukeKupacaOpensTheCustomerMessages() {
        goTo("Poruke kupaca")
        compose.onNodeWithText("Pošalji novu poruku").assertIsDisplayed()
    }

    @Test
    fun porukeAdminaOpensTheAdminMessages() {
        goTo("Poruke admina")
        compose.onNodeWithText("Pošalji novu poruku").assertIsDisplayed()
    }

    @Test
    fun interneOpensTheInternalMessages() {
        goTo("Interne poruke")
        compose.onNodeWithText("Pošalji novu poruku").assertIsDisplayed()
    }

    @Test
    fun korisnickiProfilOpensTheProfile() {
        goTo("Korisnički profil", "Opšte")
        compose.onNodeWithText("Spasi izmjene").assertExists()
    }

    @Test
    fun bellOpensTheNotifications() {
        compose.onNodeWithTag(DASHBOARD_NOTIFICATIONS_TAG).performClick()

        compose.waitUntilDoesNotExist(hasText(VENDOR_HOME), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue(
            "no notifications list and no \"Nema obavijesti\"",
            exists(hasText("Sve")) || exists(hasText("Nema obavijesti"))
        )
    }

    @Test
    fun odjaviSeGoesBackToTheLanding() {
        openDrawer()
        compose.onNode(drawerItem("Odjavi se")).performClick()

        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SERVER_TIMEOUT_MS)
        compose.onNodeWithText("Dobavljač").assertExists()
    }

    private fun profile(): Vendor {
        val result = runBlocking { DashRepository().getProfile().last() }
        assertTrue("supplier profile: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as Vendor
    }
}
