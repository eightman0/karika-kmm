package karika.distribucija.ba.ui.view.prelogin.landing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.view.prelogin.FakeLandingComponent
import kotlin.test.assertEquals
import org.junit.Test

class LandingViewTest : KarikaUiTest() {

    private val component = FakeLandingComponent()

    private fun show() {
        compose.setContent { LandingView(component) }
    }

    @Test
    fun showsBothLoginChoices() {
        show()

        compose.onNodeWithText("Prijavi se ili registruj kao:").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kupac").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dobavljač").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun kupacOpensCustomerLogin() {
        show()

        compose.onNodeWithText("Kupac").performScrollTo().performClick()

        assertEquals(listOf(KarikaType.SHOP), component.loginRequests)
    }

    @Test
    fun dobavljacOpensVendorLogin() {
        show()

        compose.onNodeWithText("Dobavljač").performScrollTo().performClick()

        assertEquals(listOf(KarikaType.VENDOR), component.loginRequests)
    }

    @Test
    fun guestSkipsLogin() {
        show()

        compose.onNodeWithText("Nastavi kao gost").performScrollTo().performClick()

        assertEquals(1, component.guestRequests)
        assertEquals(emptyList(), component.loginRequests)
    }

    @Test
    fun loadsBannersOnceWhenShown() {
        show()
        compose.waitForIdle()

        assertEquals(1, component.bannerLoads)
    }

    @Test
    fun hidesVendorLogosWhileThereAreNone() {
        show()

        compose.onNodeWithText("Dobavljači").assertDoesNotExist()
    }
}
