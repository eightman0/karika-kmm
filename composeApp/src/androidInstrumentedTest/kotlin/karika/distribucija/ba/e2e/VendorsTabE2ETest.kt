package karika.distribucija.ba.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.api.VendorRepository
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.view.shop.vendor.VENDOR_FILTER_TAG
import karika.distribucija.ba.ui.view.shop.vendor.featuredVendorTag
import karika.distribucija.ba.ui.view.shop.vendor.vendorCardTag
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end test of the customer's "Dobavljači" tab on stage.karika.ba, see [StageE2ETest]:
 * the list, paging, a vendor's page, searching, the region filter and the featured vendors.
 * What the tab should show is read from the same stage endpoints the app uses.
 */
@OptIn(ExperimentalTestApi::class)
class VendorsTabE2ETest : StageE2ETest() {

    private lateinit var firstPage: List<Vendor>

    @Before
    fun openVendorsTab() {
        logInAsCustomer()
        firstPage = vendors()
        assumeTrue("stage has no vendors", firstPage.isNotEmpty())

        compose.onNode(bottomTab("Dobavljači")).performClick()
        compose.waitUntilAtLeastOneExists(hasTestTag(vendorCardTag(firstPage.first())), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    @Test
    fun showsTheVendorsFromTheBackend() {
        compose.onNodeWithText("DOBAVLJAČI").assertIsDisplayed()
        compose.onNode(searchField).assertIsDisplayed()
        firstPage.forEach { vendor ->
            vendorList().performScrollToNode(hasTestTag(vendorCardTag(vendor)))
            compose.onNodeWithTag(vendorCardTag(vendor)).assertIsDisplayed()
            assertTrue("\"${vendor.name()}\" is not shown", exists(hasText(vendor.name())))
        }
        compose.onNodeWithText(NO_RESULTS).assertDoesNotExist()
    }

    @Test
    fun theEndOfTheListLoadsTheNextPage() {
        assumeTrue("stage has only one page of vendors", firstPage.size == PAGE_SIZE)
        val next = vendors(page = 2).firstOrNull { it !in firstPage }
        assumeTrue("the second page has no new vendor", next != null)

        vendorList().performScrollToNode(hasTestTag(vendorCardTag(firstPage.last())))

        compose.waitUntilAtLeastOneExists(hasTestTag(vendorCardTag(next!!)), SERVER_TIMEOUT_MS)
        vendorList().performScrollToNode(hasTestTag(vendorCardTag(next)))
        compose.onNodeWithTag(vendorCardTag(next)).assertIsDisplayed()
    }

    @Test
    fun tappingAVendorOpensItsPage() {
        val vendor = firstPage.first()

        compose.onNodeWithTag(vendorCardTag(vendor)).performClick()

        compose.waitUntilDoesNotExist(hasTestTag(VENDOR_FILTER_TAG), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        // In the top bar and on the page
        assertTrue(exists(hasText(vendor.name())))

        pressBack()
        compose.waitUntilAtLeastOneExists(hasTestTag(VENDOR_FILTER_TAG), SCREEN_TIMEOUT_MS)
    }

    @Test
    fun searchShowsTheVendorsMatchingTheName() {
        val vendor = firstPage.first { it.name().length > 2 }
        val expected = vendors(search = vendor.name())
        assertTrue("stage does not find \"${vendor.name()}\" by its name", vendor in expected)

        compose.onNode(searchField).performTextInput(vendor.name())

        val hidden = firstPage.filter { it !in expected }
        compose.waitUntil(SERVER_TIMEOUT_MS) { hidden.none { exists(vendorCardTag(it)) } }
        waitUntilLoaded()
        expected.forEach { match ->
            vendorList().performScrollToNode(hasTestTag(vendorCardTag(match)))
        }

        // Emptying the search shows every vendor again
        compose.onNode(hasSetTextAction() and hasText(vendor.name())).performTextClearance()
        hidden.firstOrNull()?.let { back ->
            compose.waitUntil(SERVER_TIMEOUT_MS) { exists(vendorCardTag(back)) }
        }
    }

    @Test
    fun searchWithoutAMatchShowsNoResults() {
        compose.onNode(searchField).performTextInput("zzqxv nema takvog")

        compose.waitUntilAtLeastOneExists(hasText(NO_RESULTS), SERVER_TIMEOUT_MS)
        compose.onNodeWithTag(vendorCardTag(firstPage.first())).assertDoesNotExist()
    }

    @Test
    fun regionFilterShowsTheRegionsVendorsAndItsChipRemovesIt() {
        val region = regions().first()
        val expected = vendors(region = region)

        openFilter()
        pickRegion(region)
        compose.onNodeWithText("Filtriraj").performClick()

        compose.waitUntilAtLeastOneExists(hasText("Uključeni filter:", substring = true), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
        if (expected.isEmpty()) {
            compose.waitUntilAtLeastOneExists(hasText(NO_RESULTS), SERVER_TIMEOUT_MS)
        } else {
            expected.forEach { vendorList().performScrollToNode(hasTestTag(vendorCardTag(it))) }
        }
        firstPage.filter { it !in expected }.forEach {
            compose.onNodeWithTag(vendorCardTag(it)).assertDoesNotExist()
        }

        // Tapping the chip, at the top of the list, takes the region out again. Reaching the end
        // of the list loads its next page, and the loader takes taps until it is done. The list
        // merges its text into one node, so the tap goes to the chip's own text.
        vendorList().performScrollToIndex(0)
        waitUntilLoaded()
        compose.onNodeWithText(region.label(), useUnmergedTree = true).performClick()
        compose.waitUntilDoesNotExist(hasText("Uključeni filter:", substring = true), SCREEN_TIMEOUT_MS)
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(vendorCardTag(firstPage.first())) }
    }

    @Test
    fun odustaniLeavesTheListUnfiltered() {
        val region = regions().first()

        openFilter()
        pickRegion(region)
        compose.onNodeWithText("Odustani").performClick()

        compose.waitUntilDoesNotExist(hasText("REGIJE"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Uključeni filter:", substring = true).assertDoesNotExist()
        compose.onNodeWithTag(vendorCardTag(firstPage.first())).assertExists()
    }

    @Test
    fun featuredVendorsMatchTheBackendAndOpenTheVendor() {
        val featured = promotedVendors().filter { it.promoteVendorInList }
        if (featured.isEmpty()) {
            compose.onNodeWithText("ISTAKNUTI DOBAVLJAČI").assertDoesNotExist()
            return
        }

        compose.onNodeWithText("ISTAKNUTI DOBAVLJAČI").assertIsDisplayed()
        compose.onNodeWithTag(featuredVendorTag(featured.first())).assertIsDisplayed().performClick()

        compose.waitUntilDoesNotExist(hasTestTag(VENDOR_FILTER_TAG), SCREEN_TIMEOUT_MS)
        assertTrue(exists(hasText(featured.first().name())))
    }

    private val searchField = hasSetTextAction() and hasText("Pretraži dobavljače..")

    /** The vendor list; the filter row at its top scrolls away, the vendor cards stay. */
    private fun vendorList() =
        compose.onNode(hasScrollToNodeAction() and hasAnyDescendant(hasTestTagStartingWith("vendor_")))

    private fun hasTestTagStartingWith(prefix: String) =
        SemanticsMatcher("TestTag starts with '$prefix'") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
        }

    private fun openFilter() {
        compose.onNodeWithTag(VENDOR_FILTER_TAG).performClick()
        compose.waitUntilAtLeastOneExists(hasText("REGIJE"), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText("Svi regioni").assertIsDisplayed()
    }

    private fun pickRegion(region: KarikaUnit) {
        compose.waitUntilAtLeastOneExists(hasText(region.label()), SCREEN_TIMEOUT_MS)
        compose.onNodeWithText(region.label()).performClick()
    }

    private fun exists(tag: String) = exists(hasTestTag(tag))

    private fun exists(matcher: SemanticsMatcher) =
        compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    /** One page of vendors, from the same request the tab makes. */
    private fun vendors(page: Int = 1, search: String = "", region: KarikaUnit? = null): List<Vendor> {
        val result = runBlocking {
            VendorRepository().vendors(
                searchText = search,
                pageSize = PAGE_SIZE,
                currentPage = page,
                filterValue = region?.unit().orEmpty()
            ).last()
        }
        assertTrue("vendors: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return ((result as ResultState.Success<*>).data as List<Vendor>).distinct()
    }

    private fun regions(): List<KarikaUnit> {
        val result = runBlocking { UserRepository().config().last() }
        assertTrue("config: $result", result is ResultState.Success)
        return ((result as ResultState.Success<*>).data as Config).customerRegionList.also {
            assumeTrue("stage has no regions", it.isNotEmpty())
        }
    }

    private fun promotedVendors(): List<PromotedVendor> {
        val result = runBlocking { ProductRepository().promotedVendors().last() }
        assertTrue("promoted vendors: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return (result as ResultState.Success<*>).data as List<PromotedVendor>
    }

    private companion object {
        /** CommonComponent.pageSize, what the tab loads at a time */
        const val PAGE_SIZE = 30
        const val NO_RESULTS = "Nema rezultata."
    }
}
