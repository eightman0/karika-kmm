package karika.distribucija.ba.ui.view.shop.cart.success

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.testutil.KarikaUiTest
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class CartSuccessViewTest : KarikaUiTest() {

    private class FakeCartSuccessComponent(orderId: String) : CartSuccessComponent {
        override val orderId = MutableStateFlow(orderId)
        var finishRequests = 0

        override fun finish() {
            finishRequests++
        }
    }

    @Test
    fun confirmsTheOrderWithItsNumber() {
        compose.setContent { CartSuccessView(FakeCartSuccessComponent("000123")) }

        compose.onNodeWithText("Vaš zahtjev za narudžbu je uspješno poslan dobavljačima.").assertIsDisplayed()
        compose.onNodeWithText("Broj Vaše narudžbe je: 000123").assertIsDisplayed()
        compose.onNodeWithText(
            "Poslat ćemo Vam e-poštom potvrdu narudžbe s detaljima i informacijama o praćenju."
        ).assertIsDisplayed()
    }

    @Test
    fun nastaviKupovatiFinishes() {
        val component = FakeCartSuccessComponent("000123")
        compose.setContent { CartSuccessView(component) }

        compose.onNodeWithText("Nastavi kupovati").performClick()

        assertEquals(1, component.finishRequests)
    }
}
