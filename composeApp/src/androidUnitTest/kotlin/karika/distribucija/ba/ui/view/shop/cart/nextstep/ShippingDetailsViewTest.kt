package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.Attributes
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.testProduct
import kotlin.test.assertEquals
import org.junit.Test

class ShippingDetailsViewTest : KarikaUiTest() {

    private val titova = Address(
        id = 10, street = listOf("Titova 1"), city = "Sarajevo", postcode = "71000", telephone = "061111111"
    )
    private val zmaja = Address(
        id = 11, street = listOf("Zmaja od Bosne 5"), city = "Sarajevo", postcode = "71000", telephone = "061222222"
    )
    private val customer = UserDetails(
        customAttributes = listOf(Attributes(attributeCode = "b2b_pravno_lice", value = "Karika d.o.o."))
    )
    private val sok = testProduct(id = "1", name = "Sok od jabuke 1L", price = 10.0)

    private fun show(): FakeShippingDetailsComponent {
        val component = FakeShippingDetailsComponent(
            customer = customer,
            addresses = listOf(titova, zmaja),
            selected = titova.id,
            items = mapOf(Vendor(entityId = 7, publicName = "Mljekara d.o.o.") to listOf(sok to 2)),
        )
        compose.setContent { ShippingDetailsView(component) }
        return component
    }

    private fun radioOf(address: Address) =
        compose.onNode(isSelectable() and hasAnyAncestor(hasTestTag(ShippingTestTags.address(address.id))))

    private fun footer(title: String) = compose.onNodeWithText(title)

    private fun openNewAddress() {
        compose.onNodeWithText("Dodaj novu adresu").performScrollTo().performClick()
    }

    private fun type(tag: String, text: String) {
        compose.onNodeWithTag(tag).performScrollTo().performTextInput(text)
    }

    private fun fillNewAddress(postal: String = "71000") {
        type(ShippingTestTags.FIRSTNAME, "Amar")
        type(ShippingTestTags.LASTNAME, "Hodzic")
        type(ShippingTestTags.CITY, "Sarajevo")
        type(ShippingTestTags.STREET, "Ferhadija 3")
        type(ShippingTestTags.POSTAL, postal)
        type(ShippingTestTags.PHONE, "061333333")
    }

    @Test
    fun listsTheAddressesWithTheDefaultOneChosen() {
        show()

        compose.onNodeWithText("Informacije za dostavu:").assertIsDisplayed()
        compose.onNodeWithText("Titova 1, Sarajevo, 71000, Bosna I Hercegovina, 061111111").assertExists()
        compose.onNodeWithText("Zmaja od Bosne 5, Sarajevo, 71000, Bosna I Hercegovina, 061222222").assertExists()
        radioOf(titova).assertIsSelected()
        radioOf(zmaja).assertIsNotSelected()
    }

    @Test
    fun anotherAddressCanBeChosen() {
        val component = show()

        compose.onNodeWithTag(ShippingTestTags.address(zmaja.id)).performClick()

        assertEquals("11", component.selectedAddress.value)
        radioOf(zmaja).assertIsSelected()
        radioOf(titova).assertIsNotSelected()
    }

    @Test
    fun showsEachVendorsTotals() {
        show()

        compose.onNodeWithText("Mljekara d.o.o.").performScrollTo()
        compose.onNodeWithText("VPC: 20,00 KM").assertExists()
        compose.onNodeWithText("Ukupno sa PDV: 23,40 KM").assertExists()
    }

    @Test
    fun finishingSendsTheChosenAddressAndTheNote() {
        val component = show()

        type(ShippingTestTags.VENDOR_NOTE, "Dostava prije 10h")
        footer("Završi narudžbu").performClick()

        assertEquals(
            listOf(FakeShippingDetailsComponent.Submit(false, "10", "Dostava prije 10h")),
            component.submits
        )
    }

    @Test
    fun newAddressFormNeedsEveryField() {
        val component = show()

        openNewAddress()

        assertEquals(true, component.newAddress.value)
        assertEquals("", component.selectedAddress.value)
        footer("Spasi i nastavi dalje").assertIsNotEnabled()
        compose.onNodeWithText("Završi narudžbu").assertDoesNotExist()
    }

    @Test
    fun newAddressCompanyIsTheCustomersAndCannotBeChanged() {
        show()

        openNewAddress()

        compose.onNodeWithTag(ShippingTestTags.COMPANY).performScrollTo()
            .assert(hasText("Karika d.o.o."))
            .assertIsNotEnabled()
    }

    @Test
    fun completeNewAddressCanBeSaved() {
        val component = show()
        openNewAddress()

        fillNewAddress()
        footer("Spasi i nastavi dalje").assertIsEnabled().performClick()

        assertEquals(listOf(FakeShippingDetailsComponent.Submit(true, "", "")), component.submits)
    }

    @Test
    fun newAddressNeedsAFiveDigitPostalCode() {
        show()
        openNewAddress()

        fillNewAddress(postal = "7100")

        footer("Spasi i nastavi dalje").assertIsNotEnabled()
    }

    @Test
    fun choosingAnExistingAddressClosesTheNewAddressForm() {
        val component = show()
        openNewAddress()

        compose.onNodeWithTag(ShippingTestTags.address(titova.id)).performClick()

        assertEquals(false, component.newAddress.value)
        compose.onNodeWithTag(ShippingTestTags.FIRSTNAME).assertDoesNotExist()
        footer("Završi narudžbu").assertIsEnabled()
    }

    @Test
    fun odustaniGoesBackToTheCart() {
        val component = show()

        compose.onNodeWithText("Odustani").performClick()

        assertEquals(1, component.backRequests)
    }
}
