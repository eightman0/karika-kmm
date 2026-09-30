package karika.distribucija.ba.e2e

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
 * Every test starts from a fresh, logged-out app, so the landing screen is the first thing shown.
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
        instrumentation.targetContext
            .getSharedPreferences("instance_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
        // Otherwise the system permission dialog covers the app on first launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntilAtLeastOneExists(hasText(LANDING_TITLE, substring = true), SCREEN_TIMEOUT_MS)
        waitUntilLoaded()
    }

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

    /** Closes the keyboard and waits until it is gone and the screen has settled. */
    protected fun closeKeyboard() {
        scenario.onActivity {
            WindowCompat.getInsetsController(it.window, it.window.decorView).hide(WindowInsetsCompat.Type.ime())
        }
        compose.waitUntil(SCREEN_TIMEOUT_MS) { !keyboardShown() }
        compose.waitForIdle()
    }

    private fun keyboardShown(): Boolean {
        var shown = false
        scenario.onActivity {
            shown = ViewCompat.getRootWindowInsets(it.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        return shown
    }

    /** Logs the customer test account in from the landing screen and waits for its home screen. */
    protected fun logInAsCustomer() {
        val (email, password) = stageAccount("KARIKA_STAGE_SHOP_EMAIL", "KARIKA_STAGE_SHOP_PASSWORD")
        compose.onNodeWithText("Kupac").performScrollTo().performClick()
        compose.waitUntilAtLeastOneExists(hasText("Prijava kupac"), SCREEN_TIMEOUT_MS)
        logIn(email, password)
        compose.waitUntilAtLeastOneExists(hasText("Početna"), SERVER_TIMEOUT_MS)
        waitUntilLoaded()
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

    protected companion object {
        const val LANDING_TITLE = "Vaše centralno mjesto za"
        const val SCREEN_TIMEOUT_MS = 10_000L
        const val SERVER_TIMEOUT_MS = 30_000L
        const val WRONG_LOGIN_MESSAGE = "Prijava na račun je bila pogrešna"
    }
}
