package karika.distribucija.ba.e2e

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.api.OrdersRepository
import karika.distribucija.ba.domain.api.DashRepository
import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
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
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import karika.distribucija.ba.BuildConfig
import karika.distribucija.ba.MainActivity
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.api.ProductRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.AddToCart
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.CartItem
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SetShippingAddressRequest
import karika.distribucija.ba.domain.model.ShippingAddress
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.view.prelogin.login.LOGIN_EMAIL_FIELD_TAG
import karika.distribucija.ba.ui.view.prelogin.login.LOGIN_PASSWORD_FIELD_TAG
import karika.distribucija.ba.util.KarikaConfig
import kotlin.math.ceil
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
        // The tests share one app process, which keeps the last account's token in memory;
        // a freshly started app has the guest token
        HttpClientProvider.token = getEnvJwt()
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
        compose.onNodeWithTag(LOGIN_EMAIL_FIELD_TAG).performTextInput(email)
        compose.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).performTextInput(password)
        closeKeyboard()
        compose.onNodeWithText("Prijavi se").assertIsEnabled().performClick()
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
                    seen += screenTexts().filter { text ->
                        text.startsWith(WRONG_LOGIN_MESSAGE) || LOGIN_SCREEN_TEXTS.none { text.startsWith(it) }
                    }
                    compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty() ||
                        seen.any { it.startsWith(WRONG_LOGIN_MESSAGE) } ||
                        // A network error ends this attempt early
                        (attempt < LOGIN_ATTEMPTS - 1 && seen.any { it in NETWORK_ERRORS })
                }
                if (compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty()) return
                // Trying again would only keep a locked account locked, see LoginRefused
                if (seen.any { it.startsWith(WRONG_LOGIN_MESSAGE) }) throw LoginRefused()
                // The request did not reach stage: tap "Prijavi se" again, as a user would
                seen.clear()
                waitUntilLoaded()
                Thread.sleep(RETRY_PAUSES_MS[attempt])
                if (compose.onAllNodes(arrived).fetchSemanticsNodes().isNotEmpty()) return
                compose.onNodeWithText("Prijavi se").performClick()
            } catch (e: ComposeTimeoutException) {
                throw AssertionError("the login did not get through; besides the login screen it showed: $seen", e)
            }
        }
        throw AssertionError("the login did not get through; besides the login screen it showed: $seen")
    }

    /** Writes the screen's whole semantics tree to logcat (tag E2E_SCREEN), to see why a step failed. */
    protected fun dumpScreen(label: String) {
        val roots = compose.onAllNodes(isRoot(), useUnmergedTree = true)
        repeat(roots.fetchSemanticsNodes().size) { i ->
            roots[i].printToString(Int.MAX_VALUE).chunked(3000).forEach { Log.i("E2E_SCREEN", "[$label/$i] $it") }
        }
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
        // Right after typing, a focused field's keyboard may still be on its way up
        if (!keyboardShown() && fieldFocused()) {
            runCatching { compose.waitUntil(KEYBOARD_DELAY_MS) { keyboardShown() } }
        }
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

    private fun fieldFocused() = compose.onAllNodes(
        hasSetTextAction() and SemanticsMatcher.expectValue(SemanticsProperties.Focused, true)
    ).fetchSemanticsNodes().isNotEmpty()

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
        if (account in refused) throw AssertionError("$account: $LOGIN_REFUSED, so this test did not try again")
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
        try {
            logInThroughScreen()
        } catch (e: LoginRefused) {
            refused += account
            throw AssertionError("$account: $LOGIN_REFUSED", e)
        }
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

    // The cart API, as the logged-in customer

    protected fun cartIdByApi(): String {
        // POST carts/mine returns the active cart, creating it only if there is none
        val result = runBlocking { CartRepository().createCart().last() }
        assertTrue("no cart: $result", result is ResultState.Success)
        return (result as ResultState.Success<*>).data as String
    }

    protected fun addByApi(sku: String, qty: Int) {
        var result: ResultState<*>? = null
        // Right after an order stage may not have the new cart ready yet
        repeat(3) { attempt ->
            result = runBlocking {
                CartRepository().addToCart(AddToCart(CartItem(sku = sku, qty = qty, quoteId = cartIdByApi()))).last()
            }
            if (result is ResultState.Success) return
            Thread.sleep(1_000L * (attempt + 1))
        }
        throw AssertionError("could not put $sku in the cart: $result")
    }

    protected fun setQtyByApi(product: Product, qty: Int) {
        val item = currentCart().items.first { it.sku == product.sku }
        val result = runBlocking {
            CartRepository().updateCart(
                AddToCart(CartItem(sku = item.sku, qty = qty, quoteId = cartIdByApi(), itemId = item.itemId))
            ).last()
        }
        assertTrue("could not set ${product.sku} to $qty: $result", result is ResultState.Success)
    }

    protected fun emptyCartByApi() {
        currentCart().items.mapNotNull { it.itemId }.forEach {
            runBlocking { CartRepository().removeFromCart(it.toString()).last() }
        }
    }

    /**
     * Empties the customer's cart and puts in [product], as many minimum quantities as its
     * vendor's minimum order needs (with PDV, as the cart counts it). Runs as the customer.
     */
    protected fun fillCustomerCartToTheVendorMinimum(product: Product) = asAccount(Account.CUSTOMER) {
        emptyCartByApi()
        val cart = putInCart(product)
        val vendorId = cart.items.first { it.sku == product.sku }.extensionAttributes?.vendorId
        val minimum = cart.extensionAttributes?.vendors
            ?.firstOrNull { it.id?.toString() == vendorId }?.minOrderAmount()?.toDoubleOrNull() ?: 0.0
        val steps = ceil(minimum / (product.currentPrice() * product.minQty() * PDV)).toInt().coerceAtLeast(1)
        assumeTrue("the vendor's minimum takes $steps minimum quantities", steps <= MAX_MINIMUM_STEPS)
        if (steps > 1) setQtyByApi(product, steps * product.minQty())
    }

    /**
     * Puts [product] in the customer's cart and returns the cart once it shows it. Right after an
     * order stage can take the item into the old cart while it makes the new one, so the item is
     * put in again when it does not show up.
     */
    private fun putInCart(product: Product): Cart {
        repeat(CART_ATTEMPTS) {
            addByApi(product.sku!!, product.minQty())
            repeat(10) {
                val cart = currentCart()
                if (cart.items.any { it.sku == product.sku }) return cart
                Thread.sleep(1_000)
            }
        }
        throw AssertionError("${product.sku} does not show up in the customer's cart")
    }

    /**
     * Orders [product] as the customer through the API, the way "Završi narudžbu" does, with the
     * default shipping address, and returns the order's id (its entity id, which the supplier's
     * orders call order_id).
     */
    protected fun placeCustomerOrderByApi(product: Product, note: String = "E2E narudžba"): String {
        fillCustomerCartToTheVendorMinimum(product)
        return asAccount(Account.CUSTOMER) {
            val user = (runBlocking { UserRepository().get().last() } as ResultState.Success<*>).data as UserDetails
            val address = user.shippingAddress()
            assumeTrue("the customer has no default shipping address", address != null)
            val forOrder = address!!.copy(id = null, defaultShipping = null, defaultBilling = null, save = 0)
            val set = runBlocking {
                CartRepository().setAddress(
                    SetShippingAddressRequest(ShippingAddress(forOrder, forOrder, "freeshipping", "freeshipping"))
                ).last()
            }
            assertTrue("could not set the shipping address: $set", set is ResultState.Success)
            val placed = runBlocking { CartRepository().placeOrder(note).last() }
            assertTrue("could not place the order: $placed", placed is ResultState.Success)
            ((placed as ResultState.Success<*>).data as String).trim().trim('"')
        }
    }

    // Push notifications

    /** The app's own notifications; the test runs in the app's process. */
    protected fun appNotifications() = InstrumentationRegistry.getInstrumentation().targetContext
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Says where something [sender] sent through the API stopped: on stage, in the push, or in the
     * refresh of the [screen]. The test clears the app's notifications first, so any shown since
     * came with a push. [whenNoPush] adds what could explain a missing push.
     */
    protected fun pushDiagnosis(onStage: Boolean, screen: String, sender: String, whenNoPush: String = ""): String {
        val notified = appNotifications().activeNotifications.isNotEmpty()
        return when {
            !onStage -> "it is not on stage, so $sender's send did not get through"
            !notified -> "it is on stage, but no push arrived within ${PUSH_TIMEOUT_MS / 1000} s " +
                "(no notification was shown); $whenNoPush"
            else -> "the push arrived (its notification is shown), but the $screen did not refresh"
        }
    }

    /**
     * An order of one of the supplier test account's products, placed by the customer through the
     * API, as the customer's orders have it.
     */
    protected fun orderOfTheVendorsProduct(): OrdersResponse {
        val vendorId = asAccount(Account.VENDOR) {
            val profile = runBlocking { DashRepository().getProfile().last() }
            assertTrue("supplier profile: $profile", profile is ResultState.Success)
            ((profile as ResultState.Success<*>).data as Vendor).entityId
        }
        val product = asAccount(Account.CUSTOMER) {
            val result = runBlocking { ProductRepository().searchProductsByCategory(vendorId = vendorId).last() }
            @Suppress("UNCHECKED_CAST")
            ((result as? ResultState.Success<*>)?.data as? List<Product>).orEmpty()
                .firstOrNull { it.hasOnStock() && it.sku != null && it.currentPrice() > 0 }
        }
        assumeTrue("the supplier has no product in stock", product != null)
        val id = placeCustomerOrderByApi(product!!)
        return asAccount(Account.CUSTOMER) {
            val result = runBlocking { OrdersRepository().orders(sortBy = "created_at", sortDirection = "DESC").last() }
            @Suppress("UNCHECKED_CAST")
            ((result as ResultState.Success<*>).data as List<OrdersResponse>).first { it.orderId == id }
        }
    }

    /** A tab of the shop's bottom bar, which a screen can also have as a heading. */
    protected fun bottomTab(label: String) =
        hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    protected fun pressBack() {
        scenario.onActivity { (it as ComponentActivity).onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    protected enum class Account(val emailKey: String, val passwordKey: String, val type: KarikaType) {
        CUSTOMER("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD", KarikaType.SHOP),
        VENDOR("KARIKA_STAGE_DISTRIBUTER_EMAIL", "KARIKA_STAGE_DISTRIBUTER_PASSWORD", KarikaType.VENDOR),
        SALES_REP("KARIKA_STAGE_SALESREP_EMAIL", "KARIKA_STAGE_SALESREP_PASSWORD", KarikaType.VENDOR),
    }

    /**
     * Runs [block] against stage as [account] through the API, for what another account has to
     * do first: the customer orders from the supplier before the supplier can approve it. The
     * app's own session is left as it was.
     */
    protected fun <T> asAccount(account: Account, block: () -> T): T {
        val token = apiTokens.getOrPut(account) {
            val (email, password) = stageAccount(account.emailKey, account.passwordKey)
            val result = runBlocking { LoginRepository().login(LoginDto(email, password, account.type)).last() }
            assertTrue("$account could not log in through the API: $result", result is ResultState.Success)
            (result as ResultState.Success<*>).data as String
        }
        val before = HttpClientProvider.token
        HttpClientProvider.token = token
        try {
            return block()
        } finally {
            HttpClientProvider.token = before
        }
    }

    protected companion object {
        /** API tokens of the accounts [asAccount] ran as in this run. */
        private val apiTokens = mutableMapOf<Account, String>()
        /** The cart counts the vendor's minimum with PDV. */
        private const val PDV = 1.17
        private const val MAX_MINIMUM_STEPS = 40
        private const val CART_ATTEMPTS = 3
        /** The sessions the app saved after each account's first login in this run. */
        private val sessions = mutableMapOf<Account, Map<String, String>>()
        /** Accounts whose right password stage refused in this run, see [LoginRefused]. */
        private val refused = mutableSetOf<Account>()
        private const val LOGIN_REFUSED = "stage refused the right password: the account is " +
            "locked for a while after too many failed logins (Magento says the same as for a " +
            "wrong password)"
        const val LANDING_TITLE = "Vaše centralno mjesto za"
        const val SCREEN_TIMEOUT_MS = 10_000L
        /** On stage the push usually arrives within seconds, but its queue can lag half a minute and more. */
        const val PUSH_TIMEOUT_MS = 60_000L
        /** How long a focused field's keyboard may take to come up. */
        private const val KEYBOARD_DELAY_MS = 1_500L
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
            "Prijava ", "Dobrodošli nazad", "Email adresa", "Šifra", "Zapamti me", "Zaboravili ste šifru?", "Prijavi se",
            "Nemate račun?", "stage"
        )
        /** Where a supplier lands after login: the analytics overview. */
        const val VENDOR_HOME = "Analitika — Pregled"
        /** Where a sales rep lands after login: the orders. */
        const val SALES_REP_HOME = "Upravljanje narudžbama"
    }
}

/**
 * Stage refused a login with the right password. Magento then answers as for a wrong password,
 * which the app shows as WRONG_LOGIN_MESSAGE: after too many failed logins it locks the account
 * for a while, and every further attempt keeps it locked.
 */
private class LoginRefused : RuntimeException()

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
