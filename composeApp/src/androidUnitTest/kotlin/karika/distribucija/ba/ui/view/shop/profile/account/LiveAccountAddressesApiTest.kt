package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.testutil.LiveShopTest
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The addresses on the account screen of the customer test account against the real
 * backend, through the real AccountView and AccountComponent. The customer's addresses are
 * put back as they were after every test.
 */
class LiveAccountAddressesApiTest : LiveShopTest() {

    private lateinit var component: AccountComponent
    private var original: List<Address>? = null

    @After
    fun restoreAddresses() {
        val addresses = original ?: return
        val current = customerOnServer()
        if (current.addresses != addresses) {
            runBlocking { UserRepository().put(UpdateCustomerRequest(current.copy(addresses = addresses))).last() }
        }
    }

    private fun customerOnServer(): UserDetails {
        val result = runBlocking { UserRepository().get().last() }
        return assertIs<ResultState.Success<*>>(result, "could not read the customer: $result").data as UserDetails
    }

    private fun showAccount() {
        original = customerOnServer().addresses
        loadCustomer()
        component = AccountComponent(componentContext(), stateHolder)
        compose.setContent { AccountView(component) }
    }

    /** Changes the phone in the open address form and saves it. */
    private fun savePhone(phone: String) {
        compose.onNodeWithTag(AccountTestTags.FORM_PHONE).performScrollTo().performTextClearance()
        compose.onNodeWithTag(AccountTestTags.FORM_PHONE).performTextInput(phone)
        compose.onNodeWithText("Sačuvaj izmjene").performScrollTo().performClick()
        waitForServer { component.editAddress.value == null || snackbarMessage() != null }
    }

    private fun newPhone() = "0616${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}"

    @Test
    fun billingAddressPhoneIsSaved() {
        showAccount()
        val billing = stateHolder.customerSpecificHandler.userDetails.value.billingAddress()
        assumeTrue("the test account has no billing address", billing != null)
        val phone = newPhone()

        compose.onNodeWithTag(AccountTestTags.EDIT_BILLING).performScrollTo().performClick()
        compose.onNodeWithText("Informacije za naplatu").assertExists()
        savePhone(phone)

        assertNull(snackbarMessage(), "saving failed")
        assertEquals(phone, customerOnServer().addresses.first { it.id == billing!!.id }.telephone)
    }

    @Test
    fun shippingAddressPhoneIsSaved() {
        showAccount()
        val shipping = stateHolder.customerSpecificHandler.userDetails.value.shippingAddress()
        assumeTrue("the test account has no default shipping address", shipping != null)
        val phone = newPhone()

        compose.onNodeWithTag(AccountTestTags.EDIT_SHIPPING).performScrollTo().performClick()
        savePhone(phone)

        assertNull(snackbarMessage(), "saving failed")
        val saved = customerOnServer().addresses.first { it.id == shipping!!.id }
        assertEquals(phone, saved.telephone)
        assertEquals(shipping!!.street, saved.street, "the rest of the address changed")
    }

    @Test
    fun odustaniLeavesTheAddressAsItWas() {
        showAccount()
        val shipping = stateHolder.customerSpecificHandler.userDetails.value.shippingAddress()
        assumeTrue("the test account has no default shipping address", shipping != null)

        compose.onNodeWithTag(AccountTestTags.EDIT_SHIPPING).performScrollTo().performClick()
        compose.onNodeWithTag(AccountTestTags.FORM_PHONE).performScrollTo().performTextInput("9")
        compose.onNodeWithText("Odustani").performScrollTo().performClick()

        assertNull(component.editAddress.value)
        assertEquals(shipping!!.telephone, customerOnServer().addresses.first { it.id == shipping.id }.telephone)
    }

    @Test
    fun anExtraShippingAddressCanBeDeleted() {
        // Add a plain (not default) address to delete
        val customer = customerOnServer()
        original = customer.addresses
        val template = customer.shippingAddress() ?: customer.addresses.firstOrNull()
        assumeTrue("the test account has no address to copy", template != null)
        val extra = template!!.copy(id = null, defaultShipping = null, defaultBilling = null, street = listOf("UI test 1"))
        val added = runBlocking {
            UserRepository().put(UpdateCustomerRequest(customer.copy(addresses = customer.addresses + extra))).last()
        }
        assertIs<ResultState.Success<*>>(added, "could not add an address: $added")
        val extraId = customerOnServer().addresses.firstOrNull { it.street == listOf("UI test 1") }?.id
        assertNotNull(extraId, "the added address is not on the account")

        loadCustomer()
        component = AccountComponent(componentContext(), stateHolder)
        compose.setContent { AccountView(component) }
        compose.onNodeWithTag(AccountTestTags.deleteAddress(extraId)).performScrollTo().performClick()
        compose.onNodeWithText("Obriši adresu za dostavu").assertExists()
        compose.onNode(hasText("Obriši") and hasAnyAncestor(isDialog())).performClick()
        waitForServer { customerOnServer().addresses.none { it.id == extraId } }

        compose.onNodeWithTag(AccountTestTags.deleteAddress(extraId)).assertDoesNotExist()
    }
}
