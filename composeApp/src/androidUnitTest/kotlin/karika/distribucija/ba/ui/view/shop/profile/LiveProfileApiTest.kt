package karika.distribucija.ba.ui.view.shop.profile

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.testutil.LiveShopTest
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import kotlin.test.assertEquals
import org.junit.Test

/**
 * The profile tab of the logged-in customer test account against the real backend, through
 * the real ProfileView and ProfileComponent.
 */
class LiveProfileApiTest : LiveShopTest() {

    private fun showProfile() {
        loadCustomer()
        val component = ProfileComponent(componentContext(), stateHolder)
        compose.setContent { ProfileView(component) }
    }

    private fun opens(item: String, screen: AppConfig) {
        showProfile()

        compose.onNodeWithText(item).performScrollTo().performClick()
        waitForServer { openedInApp != null }

        assertEquals(screen, openedInApp)
    }

    @Test
    fun showsTheCustomersCompany() {
        showProfile()

        compose.onNodeWithText(stateHolder.customerSpecificHandler.userDetails.value.companyName()).assertExists()
    }

    @Test
    fun mojNalogOpensTheAccount() = opens("Moj nalog", AppConfig.Account)

    @Test
    fun mojeNarudzbeOpensTheOrders() = opens("Moje narudžbe", AppConfig.Orders)

    @Test
    fun porukeAdminaOpensTheAdminMessages() = opens("Poruke admina", AppConfig.AdminMessages)

    @Test
    fun porukeDobavljacaOpensTheVendorMessages() = opens("Poruke dobavljača", AppConfig.VendorMessages)

    @Test
    fun mojiBodoviOpensThePoints() = opens("Moji bodovi", AppConfig.Points)

    @Test
    fun notifikacijeOpensTheNotifications() = opens("Notifikacije", AppConfig.Notifications)

    @Test
    fun zahtjeviZaPartnerstvoOpensThePartnershipRequests() =
        opens("Zahtjevi za partnerstvo", AppConfig.PartnershipRequests)

    @Test
    fun odjavaLogsOut() {
        showProfile()

        compose.onNodeWithText("Odjava").performScrollTo().performClick()
        waitForServer { openedInApp != null }

        val expected = if (isKiosk()) PreLoginConfig.Login(KarikaType.SHOP) else PreLoginConfig.Landing
        assertEquals(AppConfig.PreLogin(expected), openedInApp)
        assertEquals(getEnvJwt(), HttpClientProvider.token, "still logged in after Odjava")
        assertEquals("", persistence.get("JWT_TOKEN"))
    }
}
