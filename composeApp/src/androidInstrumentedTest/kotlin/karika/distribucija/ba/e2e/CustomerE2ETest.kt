package karika.distribucija.ba.e2e

import androidx.compose.ui.test.SemanticsMatcher
import karika.distribucija.ba.domain.api.CartRepository
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/**
 * Base for the logged-in customer's end-to-end tests, see [StageE2ETest]: every test starts on
 * the customer test account's home screen. Whatever a test puts into the cart is taken out again
 * afterwards; nothing else is written.
 */
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
}
