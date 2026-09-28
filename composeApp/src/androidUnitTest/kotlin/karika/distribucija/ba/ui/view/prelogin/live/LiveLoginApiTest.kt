package karika.distribucija.ba.ui.view.prelogin.live

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.testutil.FakeKarikaHandler
import karika.distribucija.ba.testutil.InMemoryPersistenceManager
import karika.distribucija.ba.testutil.KarikaUiTest
import karika.distribucija.ba.testutil.LiveTestAccounts
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.PreLoginTestTags
import karika.distribucija.ba.ui.view.prelogin.login.DefaultLoginComponent
import karika.distribucija.ba.ui.view.prelogin.login.LoginView
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Logs in against the real backend of the flavor under test (uat -> test.karika.ba), through
 * the real LoginView and DefaultLoginComponent.
 *
 * Needs an existing, approved customer test account, and optionally a vendor one; see
 * [LiveTestAccounts].
 */
class LiveLoginApiTest : KarikaUiTest() {

    private val persistence = InMemoryPersistenceManager()
    private lateinit var stateHolder: KarikaStateHolder
    private var appNavigationSubscription: Cancellation? = null

    /** The app stack the login screen asked for, once it navigates away. */
    private var appStack: List<AppConfig>? = null

    @Before
    fun setUp() {
        startKoin {
            modules(module { single<PersistenceManager> { persistence } })
        }
        stateHolder = KarikaStateHolder(FakeKarikaHandler())
        appNavigationSubscription = stateHolder.appNavigation.subscribe { event ->
            appStack = event.transformer(emptyList())
        }
    }

    @After
    fun tearDown() {
        appNavigationSubscription?.cancel()
        // The token lives in a process-wide object; put the guest token back for the next test
        HttpClientProvider.token = getEnvJwt()
        stopKoin()
    }

    private fun loginScreen(userType: KarikaType): DefaultLoginComponent {
        val lifecycle = LifecycleRegistry()
        lifecycle.resume()
        val component = DefaultLoginComponent(DefaultComponentContext(lifecycle), stateHolder, userType)
        compose.setContent { LoginView(component) }
        return component
    }

    private fun logIn(email: String, password: String) {
        compose.onNodeWithTag(PreLoginTestTags.LOGIN_EMAIL).performTextInput(email)
        compose.onNodeWithTag(PreLoginTestTags.LOGIN_PASSWORD).performTextInput(password)
        compose.onNodeWithText("Prijavi se").performScrollTo().performClick()
    }

    private fun snackbarMessage() = stateHolder.hostState.currentSnackbarData?.visuals?.message

    private fun waitForServer(condition: () -> Boolean) {
        compose.waitUntil(timeoutMillis = SERVER_TIMEOUT_MS, condition = condition)
    }

    @Test
    fun customerLogsInAndLandsOnShop() {
        val (shopEmail, shopPassword) = LiveTestAccounts.requireShop()
        val component = loginScreen(KarikaType.SHOP)
        component.rememberMe.value = true

        logIn(shopEmail, shopPassword)
        waitForServer { appStack != null || snackbarMessage() != null }

        assertNull(snackbarMessage(), "login was rejected")
        assertEquals(listOf<AppConfig>(AppConfig.Main), appStack)
        assertNotEquals(getEnvJwt(), HttpClientProvider.token, "still on the guest token")
        assertTrue(persistence.get("JWT_TOKEN").isNotEmpty(), "JWT was not saved")
        assertEquals(shopEmail, persistence.get("user_usernameSHOP"), "credentials not remembered")
    }

    @Test
    fun wrongPasswordIsRejected() {
        val (shopEmail, shopPassword) = LiveTestAccounts.requireShop()
        loginScreen(KarikaType.SHOP)

        logIn(shopEmail, shopPassword + "-wrong")
        waitForServer { snackbarMessage() != null || appStack != null }

        assertEquals(
            "Prijava na račun je bila pogrešna ili je Vaš račun privremeno onemogućen. " +
                    "Molimo pričekajte i pokušajte ponovo kasnije.",
            snackbarMessage()
        )
        assertNull(appStack)
        assertEquals(getEnvJwt(), HttpClientProvider.token)
    }

    @Test
    fun customerAccountIsSentToCustomerLogin() {
        val (shopEmail, shopPassword) = LiveTestAccounts.requireShop()
        loginScreen(KarikaType.VENDOR)

        logIn(shopEmail, shopPassword)
        waitForServer { snackbarMessage() != null || appStack != null }

        assertEquals(
            "Ovaj $shopEmail račun je napravljen samo za kupca, prijavite se kao kupac",
            snackbarMessage()
        )
        assertNull(appStack)
    }

    @Test
    fun vendorLogsInAndLandsOnDashboard() {
        val (vendorEmail, vendorPassword) = LiveTestAccounts.requireVendor()
        loginScreen(KarikaType.VENDOR)

        logIn(vendorEmail, vendorPassword)
        waitForServer { appStack != null || snackbarMessage() != null }

        assertNull(snackbarMessage(), "login was rejected")
        // A sales employee of the vendor goes to the sales-rep app instead
        val destination = appStack?.singleOrNull()
        assertTrue(
            destination == AppConfig.Dashboard || destination == AppConfig.SalesRep,
            "unexpected destination $appStack"
        )
    }

    @Test
    fun forgotPasswordIsAcceptedForExistingAccount() {
        val (shopEmail, _) = LiveTestAccounts.requireShop()

        // Sends a real reset email to the test account
        val result = runBlocking { UserRepository().forgotPass(shopEmail).last() }

        val success = assertIs<ResultState.Success<*>>(result, "server rejected the reset: $result")
        assertEquals(
            "Ako postoji nalog povezan sa '$shopEmail', dobićete e-poruku sa vezom za " +
                    "resetovanje Vaše lozinke.",
            success.data
        )
    }

    private companion object {
        const val SERVER_TIMEOUT_MS = 30_000L
    }
}
