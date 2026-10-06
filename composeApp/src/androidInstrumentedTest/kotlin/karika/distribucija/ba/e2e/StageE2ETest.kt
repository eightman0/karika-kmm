package karika.distribucija.ba.e2e

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import karika.distribucija.ba.BuildConfig
import karika.distribucija.ba.MainActivity
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.util.KarikaConfig
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base for the end-to-end tests: launches the real app (MainActivity, Koin, the real backend)
 * and taps through it as a user would. Written for the stage flavor, which talks to
 * stage.karika.ba:
 *
 *     ./gradlew :composeApp:connectedStageDebugAndroidTest
 *
 * To watch a run, the argument slowMs pauses before every step, for example
 * -Pandroid.testInstrumentationRunnerArguments.slowMs=800.
 *
 * Every test starts from a fresh, logged-out app, so the landing screen is the first thing shown.
 * A test that needs an account opens it with logInAsCustomer, logInAsVendor or logInAsSalesRep,
 * which log in through the screen once per run and then reuse that session.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
abstract class StageE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    protected lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun launchLoggedOut() {
        assumeTrue("written for the stage flavor, not ${BuildConfig.FLAVOR}", BuildConfig.FLAVOR == "stage")

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Without a saved session the app opens on the landing screen
        appPrefs().edit().clear().commit()
        // Otherwise the system permission dialogs cover the app: notifications on first launch,
        // location as soon as the sales rep dashboard opens
        val permissions = listOfNotNull(
            Manifest.permission.POST_NOTIFICATIONS.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU },
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        permissions.forEach {
            instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName, it)
        }

        InstrumentationRegistry.getArguments().getString("slowMs")?.toLongOrNull()
            ?.takeIf { it > 0 }
            ?.let { compose.registerIdlingResource(SlowMotion(it)) }

        launchApp()
        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun appPrefs() = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("instance_prefs", Context.MODE_PRIVATE)

    @After
    fun close() {
        if (::scenario.isInitialized) scenario.close()
    }

    /**
     * Waits for the loading to finish. While the app loads, a full-screen loader takes every
     * tap, so tapping earlier does nothing, for a user as for the test.
     */
    protected fun waitUntilLoaded() {
        compose.waitUntilDoesNotExist(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate),
            SERVER_TIMEOUT_MS
        )
    }

    /**
     * A stage test account as email to password, from the instrumentation arguments that
     * composeApp/build.gradle.kts fills from the environment or ~/.gradle/gradle.properties
     * (never the repo). Skips the calling test when the account is not configured.
     */
    protected fun stageAccount(emailKey: String, passwordKey: String): Pair<String, String> {
        val args = InstrumentationRegistry.getArguments()
        val email = args.getString(emailKey).orEmpty()
        val password = args.getString(passwordKey).orEmpty()
        assumeTrue("$emailKey / $passwordKey not set", email.isNotEmpty() && password.isNotEmpty())
        return email to password
    }

    /**
     * Fills in the open login screen and taps "Prijavi se". The login screen moves up with the
     * keyboard, so the keyboard is closed first; a tap while it is still opening would land
     * where the button was.
     */
    protected fun logIn(email: String, password: String) {
        compose.onNode(hasSetTextAction() and hasText("Email Adresa")).performTextInput(email)
        compose.onNode(hasSetTextAction() and hasText("Šifra")).performTextInput(password)
        closeKeyboard()
        compose.onNodeWithText("Prijavi se").performScrollTo().assertIsEnabled().performClick()
    }

    /**
     * Waits for the first screen after a login. When it does not come, the failure says what
     * the screen shows instead, such as the backend's message about the login.
     */
    protected fun waitAfterLogIn(arrived: SemanticsMatcher) {
        // Messages only stay up for a few seconds, so they are collected while waiting
        val seen = linkedSetOf<String>()
        repeat(LOGIN_ATTEMPTS) { attempt ->
            try {
                compose.waitUntil(SERVER_TIMEOUT_MS) {
                    seen += screenTexts().filter { text -> LOGIN_SCREEN_TEXTS.none { text.startsWith(it) } }
                    compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty() ||
                        // A network error ends this attempt early
                        (attempt < LOGIN_ATTEMPTS - 1 && seen.any { it in NETWORK_ERRORS })
                }
                if (compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty()) return
                // The request did not reach stage: tap "Prijavi se" again, as a user would
                seen.clear()
                waitUntilLoaded()
                Thread.sleep(RETRY_PAUSES_MS[attempt])
                if (compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty()) return
                compose.onNodeWithText("Prijavi se").performScrollTo().performClick()
            } catch (e: ComposeTimeoutException) {
                throw AssertionError("the login did not get through; besides the login screen it showed: $seen", e)
            }
        }
        throw AssertionError("the login did not get through; besides the login screen it showed: $seen")
    }

    /** Every text on screen, from the unmerged tree. */
    protected fun screenTexts(): List<String> =
        compose.onAllNodes(hasText("", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text } }

    /**
     * Closes the keyboard, wherever it was opened (a bottom sheet is a window of its own), the
     * way a user does: with the back key, which closes the keyboard first.
     */
    protected fun closeKeyboard() {
        if (keyboardShown()) {
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        }
        compose.waitUntil(SCREEN_TIMEOUT_MS) { !keyboardShown() }
        compose.waitForIdle()
    }

    /** Presses the device's back key, which goes to the focused window (a menu, a sheet, the app). */
    protected fun pressBackKey() {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()
    }

    private fun keyboardShown(): Boolean {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        return automation.windows.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
    }

    /**
     * Opens the shop as the customer test account, on its home screen. See [enterAs]: only the
     * first test of a run logs in through the screen.
     */
    protected fun logInAsCustomer() = enterAs(Account.CUSTOMER, hasText("Početna")) {
        logInThroughScreen("Kupac", "Prijava kupac", "KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")
        waitAfterLogIn(hasText("Početna"))
    }

    /** Opens the supplier test account's dashboard, on the analytics overview. See [enterAs]. */
    protected fun logInAsVendor() = enterAs(Account.VENDOR, hasText(VENDOR_HOME, substring = true)) {
        logInThroughScreen("Dobavljač", "Prijava dobavljač", "KARIKA_STAGE_DISTRIBUTER_EMAIL", "KARIKA_STAGE_DISTRIBUTER_PASSWORD")
        waitAfterLogIn(hasText(VENDOR_HOME, substring = true))
    }

    /**
     * Opens the sales rep test account's dashboard, on the orders. It logs in through the
     * supplier login (the backend tells it is a sales employee), with "Komercijalista" in the
     * drawer header. See [enterAs].
     */
    protected fun logInAsSalesRep() = enterAs(Account.SALES_REP, hasText(SALES_REP_HOME)) {
        logInThroughScreen("Dobavljač", "Prijava dobavljač", "KARIKA_STAGE_SALESREP_EMAIL", "KARIKA_STAGE_SALESREP_PASSWORD")
        waitAfterLogIn(hasText("Komercijalista"))
    }

    /**
     * Opens the app logged in as [account], on the screen that shows [home]. The first time in a
     * run it logs in through the screen with "Zapamti me" on, as [logInThroughScreen] does, and
     * keeps the session the app saved. Later tests put that session back and start the app again,
     * which opens it logged in, as for a user who chose "Zapamti me". Logging out in the app does
     * not end the session on the backend, so it stays usable; should it stop working, the test
     * logs in through the screen again.
     */
    private fun enterAs(account: Account, home: SemanticsMatcher, logInThroughScreen: () -> Unit) {
        val saved = sessions[account]
        if (saved != null) {
            scenario.close()
            appPrefs().edit().clear().apply { saved.forEach { (key, value) -> putString(key, value) } }.commit()
            launchApp()
            val opened = runCatching {
                compose.waitUntil(SERVER_TIMEOUT_MS) {
                    compose.onAllNodes(home).fetchSemanticsNodes().isNotEmpty() ||
                        compose.onAllNodes(hasText(LANDING_TITLE, substring = true)).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onAllNodes(home).fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
            if (opened) {
                waitUntilLoaded()
                return
            }
            sessions.remove(account)
            scenario.close()
            appPrefs().edit().clear().commit()
            launchApp()
            compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SCREEN_TIMEOUT_MS)
            waitUntilLoaded()
        }
        logInThroughScreen()
        compose.waitUntilAtLeastOneExists(home, SERVER_TIMEOUT_MS)
        waitUntilLoaded()
        val session = appPrefs().all.mapNotNull { (key, value) -> (value as? String)?.let { key to it } }.toMap()
        // Without a remembered login the app would open on the landing screen again
        if (session["JWT_TOKEN"].orEmpty().isNotEmpty() && session["user_type"].orEmpty().isNotEmpty()) {
            sessions[account] = session
        }
    }

    /**
     * From the landing screen, opens the login for [role] and logs in with the account under the
     * given keys, with "Zapamti me" on so that the app keeps the session.
     */
    private fun logInThroughScreen(role: String, loginTitle: String, emailKey: String, passwordKey: String) {
        val (email, password) = stageAccount(emailKey, passwordKey)
        compose.onNodeWithText(role).performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText(loginTitle), SCREEN_TIMEOUT_MS)
        compose.onNode(isToggleable()).performClick().assertIsOn()
        logIn(email, password)
    }

    /** The "Karika preporučuje" products, from the same request the home screen makes. */
    protected fun recommendedProducts(): List<Product> {
        val result = runBlocking {
            ProductRepository().searchProductsByCategory(
                categoryId = "${KarikaConfig.getKarikaProductsId()}",
                currentPage = 1,
                pageSize = 12
            ).last()
        }
        assertTrue("recommended products: $result", result is ResultState.Success)
        @Suppress("UNCHECKED_CAST")
        return ((result as ResultState.Success<*>).data as List<Product>).also {
            assumeTrue("stage recommends no products", it.isNotEmpty())
        }
    }

    /** The logged-in customer's cart on stage; without an active cart it is an empty one. */
    protected fun currentCart(): Cart {
        val result = runBlocking { CartRepository().getCart().last() }
        return (result as? ResultState.Success<*>)?.data as? Cart ?: Cart()
    }

    /** A tab of the shop's bottom bar, which a screen can also have as a heading. */
    protected fun bottomTab(label: String) =
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    protected fun pressBack() {
        scenario.onActivity { (it as ComponentActivity).onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    protected enum class Account { CUSTOMER, VENDOR, SALES_REP }

    protected companion object {
        /** The sessions the app saved after each account's first login in this run. */
        private val sessions = mutableMapOf<Account, Map<String, String>>()
        const val LANDING_TITLE = "Vaše centralno mjesto za"
        const val SCREEN_TIMEOUT_MS = 10_000L
        const val SERVER_TIMEOUT_MS = 30_000L
        const val WRONG_LOGIN_MESSAGE = "Prijava na račun je bila pogrešna"
        /** Pauses before each new login attempt, longer each time: network hiccups can last a while. */
        private val RETRY_PAUSES_MS = listOf(3_000L, 6_000L, 10_000L, 15_000L)
        private val LOGIN_ATTEMPTS = RETRY_PAUSES_MS.size + 1
        /** What the app shows when a request does not reach the backend (LoginApi.kt). */
        private val NETWORK_ERRORS = setOf(
            "Nema internet konekcije, provjerite Vašu vezu i pokušajte ponovo.",
            "Došlo je do greške. Pokušajte ponovo!"
        )
        private val LOGIN_SCREEN_TEXTS = listOf(
            "Prijava ", "Email Adresa", "Šifra", "Zapamti me", "Zaboravili ste šifru?", "Prijavi se",
            "Nemate kreiran račun?", "stage"
        )
        /** Where a supplier lands after login: the analytics overview. */
        const val VENDOR_HOME = "Analitika — Pregled"
        /** Where a sales rep lands after login: the orders. */
        const val SALES_REP_HOME = "Upravljanje narudžbama"
    }
}

/**
 * Slows a run down so it can be followed by eye: the test waits for the app to be idle before
 * every step, and this keeps it busy for [pauseMs] each time.
 */
private class SlowMotion(private val pauseMs: Long) : IdlingResource {
    private var pauseEnd = 0L
    private var idleUntil = 0L

    override val isIdleNow: Boolean
        get() {
            val now = SystemClock.uptimeMillis()
            // The test asks several times in a row before one step; the pause is once per step
            if (now < idleUntil) return true
            if (pauseEnd == 0L) pauseEnd = now + pauseMs
            if (now < pauseEnd) return false
            pauseEnd = 0L
            idleUntil = now + IDLE_WINDOW_MS
            return true
        }

    private companion object {
        const val IDLE_WINDOW_MS = 30L
    }
}
