package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.PartnershipRepository
import karika.distribucija.ba.domain.api.PointsRepository
import karika.distribucija.ba.domain.model.Bonus
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's "Profil" tab on stage.karika.ba, see [CustomerE2ETest]:
 * the profile, "Moj nalog" (opened, nothing saved), orders and their details, points,
 * notifications, the message lists, partnership requests and logout. Nothing is saved, sent,
 * cancelled, reordered, accepted or rejected.
 */
@OptIn(ExperimentalTestApi::class)
class CustomerProfileE2ETest : CustomerE2ETest() {

    @Before
    fun openProfile() {
        compose.onNode(bottomTab("Profil")).performClick()
        compose.waitUntilAtLeastOneExists(hasText("Moj nalog"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    @Test
    fun profileShowsTheCompanyAndEveryButton() {
        val company = user().companyName()
        compose.waitUntilAtLeastOneExists(hasText(company), SERVER_TIMEOUT_MS)
        listOf(
            "Moj nalog", "Moje narudžbe", "Poruke admina", "Poruke dobavljača", "Moji bodovi",
            "Notifikacije", "Zahtjevi za partnerstvo", "Odjava"
        ).forEach { assertTrue("\"$it\" is missing", exists(hasText(it))) }
    }

    @Test
    fun mojNalogShowsTheAccountSections() {
        open("Moj nalog")
        listOf("Informacije profila", "Informacije za naplatu", "Zadana adresa za dostavu").forEach {
            compose.waitUntilAtLeastOneExists(hasText(it), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun promijeniLozinkuNeedsAStrongPasswordBeforePotvrdi() {
        open("Moj nalog")
        compose.onNodeWithText("Promijeni lozinku").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Unesite staru lozinku"), SCREEN_TIMEOUT_MS)

        compose.onNode(hasSetTextAction() and hasText("Stara lozinku")).performTextInput("staralozinka")
        compose.onNode(hasSetTextAction() and hasText("Nova lozinku")).performTextInput("slaba")

        compose.onNode(hasText("Potvrdi") and hasClickAction()).assertIsNotEnabled()
        compose.onNodeWithText("Zatvori").performClick()
        compose.waitUntilDoesNotExist(hasText("Unesite staru lozinku"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun mojeNarudzbeListsStagesOrdersAndOpensOne() {
        val orders = orders()
        open("Moje narudžbe")
        if (orders.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema narudžbi", substring = true), SERVER_TIMEOUT_MS)
            return
        }
        val first = orders.first()
        compose.waitUntilAtLeastOneExists(hasText("#${first.incrementId}", substring = true), SERVER_TIMEOUT_MS)

        compose.onAllNodesWithText("Vidi narudžbu").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Artikli"), SERVER_TIMEOUT_MS)
        assertTrue(exists(hasText("Narudžba ·", substring = true), unmerged = true))
    }

    @Test
    fun mojeNarudzbeFiltersByStatus() {
        assumeTrue("the customer has no orders", orders().isNotEmpty())
        open("Moje narudžbe")

        // The option in the status menu, not an order's status chip of the same text
        val option = hasText("Otkazana") and hasAnyAncestor(isPopup())
        compose.onAllNodesWithText("Sve").onFirst().performClick()
        compose.waitUntilAtLeastOneExists(option, SCREEN_TIMEOUT_MS)
        compose.onNode(option).performClick()
        compose.waitUntilDoesNotExist(option, SCREEN_TIMEOUT_MS)

        waitUntilLoaded()
        val cancelled = orders(status = "cancelled")
        if (cancelled.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema narudžbi za izabrani status."), SERVER_TIMEOUT_MS)
        } else {
            compose.waitUntilAtLeastOneExists(hasText("#${cancelled.first().incrementId}", substring = true), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun mojiBodoviShowTheBalance() {
        val bonus = points()
        open("Moji bodovi")
        compose.waitUntilAtLeastOneExists(hasText("Iznos ostvarenih bodova"), SERVER_TIMEOUT_MS)
        assertTrue(exists(hasText("Spisak trenutnih transakcija"), unmerged = true))
        assertTrue("balance ${bonus.pointBalance} is not shown", exists(hasText(" KM", substring = true), unmerged = true))
    }

    @Test
    fun notifikacijeOpenTheNotifications() {
        open("Notifikacije")
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText("Sve")) || exists(hasText("Nema obavijesti")) }
    }

    @Test
    fun porukeDobavljacaOpenTheVendorConversations() {
        open("Poruke dobavljača")
        compose.waitUntilAtLeastOneExists(hasText("Pošalji novu poruku"), SERVER_TIMEOUT_MS)
    }

    @Test
    fun zahtjeviZaPartnerstvoShowStagesRequests() {
        val requests = partnershipRequests()
        open("Zahtjevi za partnerstvo")
        if (requests.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Trenutno nemate zahtjeva za partnerstvo."), SERVER_TIMEOUT_MS)
        } else {
            compose.waitUntilAtLeastOneExists(hasText("Prihvati"), SERVER_TIMEOUT_MS)
            assertTrue(exists(hasText("Odbij")))
        }
    }

    @Test
    fun odjavaGoesBackToTheLanding() {
        compose.onNodeWithText("Odjava").performClick()

        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SERVER_TIMEOUT_MS)
    }

    private fun open(button: String) {
        compose.onNode(hasText(button) and hasClickAction()).performClick()
        compose.waitUntilDoesNotExist(hasText("Zahtjevi za partnerstvo") and hasClickAction(), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun orders(status: String = ""): List<OrdersResponse> {
        val result = runBlocking {
            OrdersRepository().orders(filterValue = status, sortBy = "created_at", sortDirection = "DESC").last()
        }
        assertTrue("orders: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<OrdersResponse>
    }

    private fun points(): Bonus {
        val result = runBlocking { PointsRepository().get().last() }
        assertTrue("points: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as Bonus
    }

    private fun partnershipRequests(): List<PartnershipRequest> {
        val result = runBlocking { PartnershipRepository().list().last() }
        assertTrue("partnership requests: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<PartnershipRequest>
    }
}
