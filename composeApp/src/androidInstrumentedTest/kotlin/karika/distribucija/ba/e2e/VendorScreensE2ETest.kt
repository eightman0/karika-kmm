package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.domain.api.ChatRepository
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.domain.model.ChatConversationSearchResults
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.DiscountRuleSearchResults
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.domain.model.VendorNotificationSearchResults
import karika.distribucija.ba.ui.components.DASHBOARD_NOTIFICATIONS_TAG
import karika.distribucija.ba.ui.components.conversationTag
import karika.distribucija.ba.ui.view.distributer.customers.CUSTOMER_RULE_TAG
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * End-to-end test of the rest of the supplier dashboard on stage.karika.ba, see [VendorE2ETest]:
 * discount rules and their editor, the message lists, notifications, the profile and the
 * analytics filters. Nothing is saved, sent or deleted; every editor and dialog is left with
 * its back link, "Odustani" or "Zatvori".
 */
@OptIn(ExperimentalTestApi::class)
class VendorScreensE2ETest : VendorE2ETest() {

    // Upravljanje rabatima

    @Test
    fun discountRulesShowTheThreeSectionsWithStagesRules() {
        val rules = rules()
        goTo("Rabati")

        listOf("Po kupcu", "Po tipu kupca", "Po regiji kupca").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true))
        }
        // One card per rule; a section without rules says so
        val sections = rules.groupBy { it.discountType }
        val emptySections = listOf("per_customer", "per_customer_group", "per_customer_region")
            .count { sections[it].isNullOrEmpty() }
        assertEquals(rules.size, count(hasTestTag(CUSTOMER_RULE_TAG)))
        assertEquals(emptySections, count(hasText("Još nema pravila", substring = true)))
    }

    @Test
    fun dodajRedOpensTheRuleEditorWhichNeedsADiscount() {
        goTo("Rabati")

        compose.onAllNodesWithText("Dodaj").onFirst().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Novo pravilo"), SCREEN_TIMEOUT_MS)
        listOf("Kupac", "Artikal ili kategorija", "Min. količina", "Rabat").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true))
        }
        // No customer means every customer ("Svi kupci"), but without a discount nothing is saved
        compose.onNodeWithText("Sačuvaj pravilo").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Rabat mora biti između 0 i 100 %."), SCREEN_TIMEOUT_MS)

        compose.onNodeWithText("Odustani").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Po kupcu"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun aRuleOpensInTheEditorForChanging() {
        assumeTrue("stage has no discount rules for this supplier", rules().isNotEmpty())
        goTo("Rabati")
        compose.waitUntil(SERVER_TIMEOUT_MS) { count(hasTestTag(CUSTOMER_RULE_TAG)) > 0 }

        compose.onAllNodes(hasTestTag(CUSTOMER_RULE_TAG)).onFirst().performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Izmjena pravila", substring = true), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Sačuvaj izmjene").assertExists()
        compose.onNodeWithText("Odustani").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Po kupcu"), SCREEN_TIMEOUT_MS)
    }

    // Poruke

    @Test
    fun customerMessagesListStagesConversations() {
        val conversations = conversations(ChatAxis.VENDOR_CUSTOMER)
        goTo("Poruke kupaca", "Poruke kupca")
        conversations.take(10).forEach {
            compose.waitUntilAtLeastOneExists(hasTestTag(conversationTag(it)), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun adminMessagesListStagesConversations() {
        val conversations = conversations(ChatAxis.VENDOR_ADMIN)
        goTo("Poruke admina")
        conversations.take(10).forEach {
            compose.waitUntilAtLeastOneExists(hasTestTag(conversationTag(it)), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun internalMessagesListStagesConversations() {
        val conversations = conversations(ChatAxis.STAFF)
        goTo("Interne poruke")
        conversations.take(10).forEach {
            compose.waitUntilAtLeastOneExists(hasTestTag(conversationTag(it)), SERVER_TIMEOUT_MS)
        }
    }

    @Test
    fun aConversationOpensWithItsMessagesAndTheBackLinkReturns() {
        val conversation = conversations(ChatAxis.VENDOR_CUSTOMER).firstOrNull()
        assumeTrue("the supplier has no customer conversations", conversation != null)
        goTo("Poruke kupaca", "Poruke kupca")

        compose.onNodeWithTag(conversationTag(conversation!!)).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Nazad na poruke"), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        assertTrue(exists(hasSetTextAction() and hasText("Napiši komentar")))
        compose.onNodeWithText("Nazad na poruke").performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag(conversationTag(conversation)), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun newCustomerMessageAsksForARecipientFirst() {
        goTo("Poruke kupaca", "Poruke kupca")

        compose.onNodeWithText("Pošalji novu poruku").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Primalac"), SCREEN_TIMEOUT_MS)
        assertTrue(exists(hasSetTextAction() and hasText("Pretražite primaoce")))
        compose.onNodeWithText("Nazad na poruke").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pošalji novu poruku"), SCREEN_TIMEOUT_MS)
    }

    // Obavijesti

    @Test
    fun notificationsListStagesNotifications() {
        val notifications = notifications()

        compose.onNodeWithTag(DASHBOARD_NOTIFICATIONS_TAG).performClick()
        compose.waitUntilDoesNotExist(hasText(VENDOR_HOME), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()

        if (notifications.items.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText("Nema obavijesti"), SCREEN_TIMEOUT_MS)
        } else {
            // A row shows the notification's text
            val first = notifications.items.first().body.take(30)
            compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(first, substring = true), unmerged = true) }
        }
        val unread = notifications.items.any { !it.isRead }
        assertEquals(unread, exists(hasText("Označi sve kao pročitano")))
    }

    // Korisnički profil

    @Test
    fun profileShowsTheSuppliersDetails() {
        val profile = profile()
        goTo("Korisnički profil", "Opšte")

        listOf("Naziv pravnog lica", "Email adresa", "Telefon", "Minimalna vrijednost narudžbe").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true))
        }
        profile.email?.let { assertTrue("$it is not shown", exists(hasText(it), unmerged = true)) }
    }

    @Test
    fun promijeniLozinkuOpensTheSheetAndZatvoriClosesIt() {
        goTo("Korisnički profil", "Opšte")
        // Promijeni lozinku is on the Postavke tab
        compose.onNodeWithText("Postavke").performClick()

        compose.onNodeWithText("Promijeni lozinku").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Unesite staru lozinku"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Zatvori").performClick()
        compose.waitUntilDoesNotExist(hasText("Unesite staru lozinku"), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun obrisiNalogAsksOnceAndOdustaniKeepsTheAccount() {
        goTo("Korisnički profil", "Opšte")
        // Obriši nalog is on the Postavke tab
        compose.onNodeWithText("Postavke").performClick()

        compose.onNode(hasText("Obriši nalog") and hasClickAction()).performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText("Jeste li sigurni da želite obrisati nalog?"), SCREEN_TIMEOUT_MS)
        assertEquals("the delete dialog is shown more than once", 1, count(hasText("Jeste li sigurni da želite obrisati nalog?")))
        compose.onAllNodesWithText("Odustani").onFirst().performClick()
        compose.waitUntilDoesNotExist(hasText("Jeste li sigurni da želite obrisati nalog?"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Promijeni lozinku").assertExists()
    }

    // Analitika

    @Test
    fun analyticsFiltersOpenAndPrimijeniReturns() {
        // The period summary row, "{from} – {to} · {grouping} · {comparison}", opens the filters
        compose.onNode(hasText("·", substring = true) and hasClickAction()).performClick()

        compose.waitUntilAtLeastOneExists(hasText("Nazad na analitiku"), SCREEN_TIMEOUT_MS)
        listOf("Period", "Grupisanje", "Poređenje").forEach {
            assertTrue("\"$it\" is not shown", exists(hasText(it), unmerged = true))
        }
        compose.onNodeWithText("Primijeni").performScrollTo().performClick()

        compose.waitUntilAtLeastOneExists(hasText(VENDOR_HOME), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun atRiskFiltersByRisk() {
        goTo("Kupci koji zahtijevaju pažnju", inAnalytics = true)

        listOf("Približava se riziku", "U riziku", "Ozbiljno kašnjenje", "Nikad naručio", "30+ dana").forEach {
            compose.onNodeWithText(it).performScrollTo().performClick()
            compose.waitForIdle()
        }
        // The title, and the drawer item of the same name
        assertTrue(exists(hasText("Kupci koji zahtijevaju pažnju")))
    }

    @Test
    fun productAnalyticsSwitchesToCategories() {
        goTo("Proizvodi i kategorije", inAnalytics = true)

        compose.onNodeWithText("Kategorije").performClick()

        compose.waitUntilAtLeastOneExists(
            hasText("Proizvod može pripadati više kategorija", substring = true),
            SERVER_TIMEOUT_MS
        )
        compose.onNodeWithText("Proizvodi").performClick()
        compose.waitUntilAtLeastOneExists(hasText("Pretraži proizvode"), SCREEN_TIMEOUT_MS)
    }

    private fun count(matcher: androidx.compose.ui.test.SemanticsMatcher) =
        compose.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().size

    private fun rules(): List<DiscountRule> {
        val result = runBlocking { DashRepository().getCustomerRules().last() }
        assertTrue("discount rules: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as DiscountRuleSearchResults).items
    }

    private fun conversations(axis: ChatAxis): List<ChatConversation> {
        val result = runBlocking { ChatRepository().getConversations(axis, pageSize = 100).last() }
        assertTrue("conversations: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as ChatConversationSearchResults).items
    }

    private fun notifications(): VendorNotificationSearchResults {
        val result = runBlocking { DashRepository().vendorNotifications(pageSize = 50).last() }
        assertTrue("notifications: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorNotificationSearchResults
    }

    private fun profile(): Vendor {
        val result = runBlocking { DashRepository().getProfile().last() }
        assertTrue("supplier profile: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as Vendor
    }
}
