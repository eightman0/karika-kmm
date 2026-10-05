package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end test of the sales rep dashboard's frame on stage.karika.ba, see [SalesRepE2ETest]:
 * the first screen, the drawer and where each of its items goes, notifications and logout.
 */
@OptIn(ExperimentalTestApi::class)
class SalesRepNavigationE2ETest : SalesRepE2ETest() {

    @Test
    fun startsOnTheOrders() {
        assertTrue(displayed(hasText(SALES_REP_HOME)))
    }

    @Test
    fun drawerShowsTheSalesRepAndEveryItem() {
        val me = me()

        openDrawer()

        assertTrue(displayed(hasText("Komercijalista")))
        me.name?.let { assertTrue("\"$it\" is not shown", displayed(hasText(it))) }
        listOf("Analitika", "Upravljanje narudžbama", "Upravljanje kupcima", "Poruke", "Odjavi se").forEach {
            assertTrue("\"$it\" is not in the drawer", displayed(drawerItem(it)))
        }
        // "Operacije" is switched off in the app
        assertTrue(!exists(hasText("Operacije")))
    }

    @Test
    fun analitikaShowsOnlyWhatTheSalesRepMaySee() {
        val canSeeDashboard = me().capabilities.canSeeDashboard

        openDrawer()
        compose.onNode(drawerItem("Analitika")).performClick()

        listOf(
            "Pregled", "Trendovi prodaje", "Analitika kupaca", "Kupci koji zahtijevaju pažnju",
            "Proizvodi i kategorije"
        ).forEach { compose.waitUntil(SCREEN_TIMEOUT_MS) { displayed(drawerItem(it)) } }
        assertEquals(canSeeDashboard, displayed(drawerItem("Komercijalisti")))
    }

    @Test
    fun pregledOpensTheAnalyticsOverview() {
        goTo("Pregled", "Analitika — Pregled", inAnalytics = true)
        compose.onNodeWithText("Ukupan prihod:").assertExists()
    }

    @Test
    fun kupciKojiZahtijevajuPaznjuOpensTheAtRiskCustomers() {
        goTo("Kupci koji zahtijevaju pažnju", inAnalytics = true)
        compose.onNodeWithText("Neaktivnost se mjeri od danas, ne od odabranog perioda.").assertExists()
    }

    @Test
    fun upravljanjeKupcimaOpensTheCustomers() {
        goTo("Upravljanje kupcima")
        assertTrue(exists(hasText("Pretraži kupce...")))
    }

    @Test
    fun porukeOpensTheThreeMessageLists() {
        goTo("Interne poruke", under = "Poruke")
        goTo("Poruke admina", under = "Poruke")
        goTo("Poruke kupaca", under = "Poruke")
        assertTrue(exists(hasText("Pošalji novu poruku")))
    }

    @Test
    fun bellOpensTheNotifications() {
        compose.onNodeWithContentDescription("Obavijesti").performClick()

        compose.waitUntil(SERVER_TIMEOUT_MS) {
            exists(hasText("Sve")) || exists(hasText("Nema obavijesti"))
        }
        waitUntilLoaded()
        // The notifications screen has a back arrow instead of the menu
        assertTrue(exists(hasContentDescription("Nazad")))
    }

    @Test
    fun odjaviSeGoesBackToTheLanding() {
        openDrawer()
        compose.onNode(drawerItem("Odjavi se")).performClick()

        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SERVER_TIMEOUT_MS)
        compose.onNodeWithText("Dobavljač").assertExists()
    }
}
