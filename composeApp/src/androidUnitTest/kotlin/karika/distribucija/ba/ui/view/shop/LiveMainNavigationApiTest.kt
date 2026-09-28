package karika.distribucija.ba.ui.view.shop

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import karika.distribucija.ba.testutil.LiveShopTest
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import org.junit.Test

/**
 * The shop's bottom bar and top bar for the logged-in customer test account, through the
 * real MainView and MainComponent against the real backend.
 */
class LiveMainNavigationApiTest : LiveShopTest() {

    private lateinit var component: MainComponent

    private fun showShop() {
        loadCustomer()
        component = MainComponent(componentContext(), stateHolder)
        compose.setContent { MainView(component) }
        compose.waitForIdle()
    }

    private fun tab(title: String) = compose.onNode(isSelectable() and hasContentDescription(title))

    private fun active() = component.stack.value.active.instance

    /** Taps the [title] tab and checks it opens [screen] and is the selected tab. */
    private fun tapTab(title: String, screen: KClass<out MainChild>) {
        tab(title).performClick()
        waitForServer { screen.isInstance(active()) }

        tab(title).assertIsSelected()
        TABS.filter { it != title }.forEach { tab(it).assertIsNotSelected() }
    }

    @Test
    fun opensOnTheHomeTab() {
        showShop()

        assertEquals(MainChild.Home::class, active()::class)
        tab("Početna").assertIsSelected()
    }

    @Test
    fun dobavljaciTabOpensTheVendors() {
        showShop()
        tapTab("Dobavljači", MainChild.Vendor::class)
    }

    @Test
    fun meniTabOpensTheMenu() {
        showShop()
        tapTab("Meni", MainChild.Menu::class)
    }

    @Test
    fun korpaTabOpensTheCart() {
        showShop()
        tapTab("Korpa", MainChild.Cart::class)
    }

    @Test
    fun profilTabOpensTheProfile() {
        showShop()
        tapTab("Profil", MainChild.Profile::class)
    }

    @Test
    fun pocetnaTabReturnsHome() {
        showShop()
        tapTab("Meni", MainChild.Menu::class)

        tapTab("Početna", MainChild.Home::class)
    }

    @Test
    fun theTopBarSearchFieldOpensSearch() {
        showShop()

        compose.onNodeWithText("Pretraži..").performClick()
        waitForServer { active() is MainChild.Search }
    }

    private companion object {
        val TABS = listOf("Početna", "Dobavljači", "Meni", "Korpa", "Profil")
    }
}
