package karika.distribucija.ba.ui.view.shop.vendor

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.vendor.details.VendorDetailsComponent
import karika.distribucija.ba.ui.view.shop.vendor.details.VendorDetailsView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.Test

/**
 * The vendors tab and a vendor's page for the customer test account against the real
 * backend, through the real VendorView, VendorDetailsView and their components.
 */
class LiveVendorsApiTest : LiveShopTest() {

    private lateinit var list: VendorComponent

    private fun showVendors(): List<Vendor> {
        list = VendorComponent(componentContext(), stateHolder)
        compose.setContent { VendorView(list) }
        waitForServer { list.vendors.value.isNotEmpty() || snackbarMessage() != null }
        assertTrue(list.vendors.value.isNotEmpty(), "no vendors, message: ${snackbarMessage()}")
        return list.vendors.value
    }

    private fun showVendor(vendor: Vendor): VendorDetailsComponent {
        val component = VendorDetailsComponent(componentContext(), stateHolder, vendor)
        compose.setContent { VendorDetailsView(component) }
        waitForServer { component.products.value.isNotEmpty() && !component.loader.value }
        return component
    }

    /** The vendor of a recommended product, so one that has products. */
    private fun vendorWithProducts(): Vendor {
        val named = recommendedProducts().first { it.vendorName() != "-" && it.vendorId().toIntOrNull() != null }
        return Vendor(entityId = named.vendorId().toInt(), publicName = named.vendorName())
    }

    @Test
    fun vendorsAreListed() {
        val vendors = showVendors()

        compose.onNodeWithText("DOBAVLJAČI").assertExists()
        compose.onNodeWithTag(vendorCardTag(vendors.first())).performScrollTo()
        compose.onAllNodesWithText(vendors.first().name()).onFirst().assertExists()
    }

    @Test
    fun searchingByNameFindsTheVendor() {
        val target = showVendors().first { it.name().length > 2 }

        compose.onNode(hasSetTextAction()).performTextInput(target.name())
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        waitForLoaded()

        val ids = list.vendors.value.map { it.entityId }
        assertTrue(target.entityId in ids, "'${target.name()}' not found: $ids")
    }

    @Test
    fun tappingAVendorOpensItsPage() {
        val vendor = showVendors().first()

        compose.onNodeWithTag(vendorCardTag(vendor)).performScrollTo().performClick()
        waitForServer { openedInShop != null }

        assertEquals(vendor.entityId, assertIs<MainConfig.VendorDetails>(openedInShop).vendor.entityId)
    }

    @Test
    fun vendorPageListsOnlyItsOwnProducts() {
        val vendor = vendorWithProducts()

        val component = showVendor(vendor)

        compose.onAllNodesWithText(vendor.name()).onFirst().assertExists()
        val others = component.products.value.filter { it.vendorId() != vendor.entityId.toString() }
        assertTrue(others.isEmpty(), "products of other vendors: ${others.map { it.vendorName() }}")
    }

    @Test
    fun vendorPageKeepsTheVendorItOpened() {
        val vendor = vendorWithProducts()

        val component = showVendor(vendor)

        // The page reloads the vendor by searching its name and taking the first match
        assertEquals(vendor.entityId, component.vendor.value.entityId, "switched to ${component.vendor.value.name()}")
    }

    @Test
    fun vendorPageSearchFindsItsProduct() {
        val vendor = vendorWithProducts()
        val component = showVendor(vendor)
        val target = component.products.value.last()

        compose.onNode(hasSetTextAction()).performTextInput(target.name())
        compose.mainClock.advanceTimeBy(DEBOUNCE_MS)
        waitForLoaded()

        val skus = component.products.value.map { it.sku }
        assertTrue(target.sku in skus, "'${target.name()}' not found at its vendor: $skus")
    }

    @Test
    fun messageButtonOpensTheConversationWithTheVendor() {
        val vendor = vendorWithProducts()
        showVendor(vendor)

        compose.onNodeWithText("Pošalji poruku dobavljaču").performScrollTo().performClick()
        waitForServer { openedInApp != null }

        assertEquals(vendor.entityId.toString(), assertIs<AppConfig.MessagesOverview>(openedInApp).conversation.vendorId)
    }

    private companion object {
        const val DEBOUNCE_MS = 600L
    }
}
