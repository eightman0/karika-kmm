package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before

/**
 * Base for the logged-in customer's end-to-end tests, see [StageE2ETest]: every test starts on
 * the customer test account's home screen. Whatever a test puts into the cart is taken out again
 * afterwards. The test account is free to change: the tests that save, order, cancel or send
 * leave their changes on stage.
 */
@OptIn(ExperimentalTestApi::class)
abstract class CustomerE2ETest : StageE2ETest() {

    /** Cart item ids that were there before the test, so only the test's own are taken out. */
    private var cartBefore: Set<Int>? = null

    @Before
    fun openShopAsCustomer() {
        logInAsCustomer()
        cartBefore = currentCart().items.mapNotNull { it.itemId }.toSet()
    }

    @After
    fun takeOutWhatTheTestAdded() {
        val before = cartBefore ?: return
        currentCart().items
            .mapNotNull { it.itemId }
            .filter { it !in before }
            .forEach { runBlocking { CartRepository().removeFromCart(it.toString()).last() } }
    }

    protected fun exists(matcher: SemanticsMatcher, unmerged: Boolean = false) =
        compose.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty()

    protected fun count(matcher: SemanticsMatcher, unmerged: Boolean = true) =
        compose.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes().size

    /** The customer as stage has it now. */
    protected fun user(): UserDetails {
        val result = runBlocking { UserRepository().get().last() }
        assertTrue("customer: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as UserDetails
    }

    /**
     * Starts the app again, logged in, on the home screen: after a test changed something
     * through the API, the app only shows it once it loads it again.
     */
    protected fun reopenApp() = logInAsCustomer()

    /**
     * Letters that differ from run to run, for values a test saves: most of the app's name and
     * address fields take letters only.
     */
    protected fun uniqueLetters(length: Int = 6): String {
        var n = System.currentTimeMillis()
        return buildString { repeat(length) { append('a' + (n % 26).toInt()); n /= 26 } }.reversed()
    }

    /** Digits that differ from run to run. */
    protected fun uniqueDigits(length: Int): String = System.currentTimeMillis().toString().takeLast(length)

    /** Replaces the text of the [index]th text field on screen (fields the form shows prefilled). */
    protected fun replaceField(index: Int, text: String) {
        compose.onAllNodes(hasSetTextAction())[index].performTextReplacement(text)
    }

    /** The current text of the [index]th text field on screen. */

    /** Opens the dropdown titled [title] and picks [option] from it. */
    protected fun pick(title: String, option: String) {
        compose.onAllNodesWithText(title).onFirst().performClick()
        val inMenu = hasText(option) and hasAnyAncestor(isPopup())
        compose.waitUntilAtLeastOneExists(inMenu, SCREEN_TIMEOUT_MS)
        compose.onNode(inMenu).performClick()
        compose.waitUntilDoesNotExist(inMenu, SCREEN_TIMEOUT_MS)
    }

    /** A dialog's button, where the screen behind the dialog has a button of the same name. */
    protected fun dialogButton(label: String) = hasText(label) and hasAnyAncestor(isDialog())

    /**
     * Waits for a snackbar with [text]. Snackbars stay up for 1.5 s only, so this polls while
     * the app works and must be called right after the action that shows it.
     */
    protected fun waitForSnackbar(text: String) {
        compose.waitUntil(SERVER_TIMEOUT_MS) { exists(hasText(text, substring = true), unmerged = true) }
    }
}
