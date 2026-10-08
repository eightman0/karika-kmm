package karika.distribucija.ba.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorOperationsMe
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before

/**
 * Base for the end-to-end tests of the dashboards with a side drawer, the supplier's and the
 * sales rep's, see [StageE2ETest]: every test starts logged in through [logInToDashboard].
 */
@OptIn(ExperimentalTestApi::class)
abstract class DashboardE2ETest : StageE2ETest() {

    /** Test tag of the top bar's menu icon, which opens the drawer. */
    protected abstract val menuTag: String

    protected abstract fun logInToDashboard()

    @Before
    fun openDashboard() {
        logInToDashboard()
    }

    /** Opens the side drawer from the top bar's menu icon. */
    protected fun openDrawer() {
        // The loader takes taps while it is up, so the tap waits for it and is repeated if lost
        repeat(3) {
            waitUntilLoaded()
            compose.onNodeWithTag(menuTag).performClick()
            val opened = runCatching {
                compose.waitUntil(SCREEN_TIMEOUT_MS / 2) { displayed(hasText("Odjavi se")) }
            }.isSuccess
            if (opened) return
        }
        throw AssertionError("the drawer did not open")
    }

    /**
     * Opens the drawer and taps the item [label] (inside the group [under], if it is in one),
     * then waits for the screen it opens, which shows [title], and for the drawer to close.
     * Drawer items share their label with the screen title, so the item is the one that can
     * be tapped.
     */
    protected fun goTo(
        label: String,
        title: String = label,
        inAnalytics: Boolean = false,
        under: String? = if (inAnalytics) "Analitika" else null
    ) {
        openDrawer()
        // The drawer remembers an opened group, and tapping it again would close it
        if (under != null && !displayed(drawerItem(label))) {
            compose.onNode(drawerItem(under)).performClick()
            compose.waitUntil(SCREEN_TIMEOUT_MS) { displayed(drawerItem(label)) }
        }
        compose.onNode(drawerItem(label)).performClick()
        compose.waitUntil(SCREEN_TIMEOUT_MS) { !displayed(hasText("Odjavi se")) }
        compose.waitUntilAtLeastOneExists(hasText(title, substring = true), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
    }

    /**
     * Scrolls the screen's list to the item matching [matcher], waiting for it to load. Lists
     * only compose the items on screen, so one further down does not exist until scrolled to.
     */
    protected fun scrollListTo(matcher: SemanticsMatcher, timeoutMs: Long = SERVER_TIMEOUT_MS) {
        compose.waitUntil(timeoutMs) {
            // Unmerged, as a tag around a tappable card is merged away
            val lists = compose.onAllNodes(hasScrollToNodeAction(), useUnmergedTree = true)
            (0 until lists.fetchSemanticsNodes().size).any {
                runCatching { lists[it].performScrollToNode(matcher) }.isSuccess
            }
        }
    }

    /** Who the logged-in account is in the supplier's team and what it may do, as the app reads it. */
    protected fun me(): VendorOperationsMe {
        val result = runBlocking { SalesRepository().getMe().last() }
        assertTrue("vendor operations me: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as VendorOperationsMe
    }

    /**
     * The [index]th text field on screen. The screens under the open one stay composed, with
     * their own fields, so only the displayed ones count.
     */
    protected fun field(index: Int): SemanticsNodeInteraction {
        val shown = shownFieldIndices()
        assertTrue("only ${shown.size} text fields are shown", index in shown.indices)
        return compose.onAllNodes(hasSetTextAction())[shown[index]]
    }

    /** The last text field on screen, such as a conversation's message field. */
    protected fun lastField() = field(shownFieldIndices().size - 1)

    protected fun shownFieldIndices(): List<Int> {
        val fields = compose.onAllNodes(hasSetTextAction())
        return (0 until fields.fetchSemanticsNodes().size).filter {
            runCatching { fields[it].assertIsDisplayed() }.isSuccess
        }
    }

    protected fun drawerItem(label: String) = hasText(label) and hasClickAction()

    /** Whether any node matching [matcher] is on screen (the closed drawer's items exist, offscreen). */
    protected fun displayed(matcher: SemanticsMatcher): Boolean {
        val nodes = compose.onAllNodes(matcher)
        return (0 until nodes.fetchSemanticsNodes().size).any {
            runCatching { nodes[it].assertIsDisplayed() }.isSuccess
        }
    }

    protected fun exists(matcher: SemanticsMatcher, unmerged: Boolean = false) =
        compose.onAllNodes(matcher, useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty()
}
