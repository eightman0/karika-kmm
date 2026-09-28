package karika.distribucija.ba.ui.view.prelogin

import android.app.Application
import android.os.Looper
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.testutil.FakeKarikaHandler
import karika.distribucija.ba.testutil.InMemoryPersistenceManager
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationComponent
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Drives the real pre-login components (no UI) through the Decompose stack, to check the
 * screens are wired to each other the way the fake-component view tests assume.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PreLoginNavigationTest {

    private val persistence = InMemoryPersistenceManager()
    private lateinit var stateHolder: KarikaStateHolder
    private lateinit var root: PreLoginComponent

    @Before
    fun setUp() {
        startKoin {
            modules(module { single<PersistenceManager> { persistence } })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun start(config: PreLoginConfig = PreLoginConfig.Landing) {
        val lifecycle = LifecycleRegistry()
        lifecycle.resume()
        stateHolder = KarikaStateHolder(FakeKarikaHandler())
        root = PreLoginComponent(DefaultComponentContext(lifecycle), stateHolder, config)
    }

    private val active get() = root.stack.value.active.instance

    private fun login(): LoginComponent = assertIs<PreLoginChild.Login>(active).component

    private fun registration(): RegistrationComponent =
        assertIs<PreLoginChild.Registration>(active).component

    @Test
    fun startsOnLanding() {
        start()

        assertIs<PreLoginChild.Landing>(active)
    }

    @Test
    fun kupacOnLandingOpensCustomerLogin() {
        start()

        assertIs<PreLoginChild.Landing>(active).component.navigateLogin(KarikaType.SHOP)

        assertTrue(login().isShop())
        assertEquals("Prijava kupac", login().title())
    }

    @Test
    fun dobavljacOnLandingOpensVendorLogin() {
        start()

        assertIs<PreLoginChild.Landing>(active).component.navigateLogin(KarikaType.VENDOR)

        assertFalse(login().isShop())
        assertEquals("Prijava dobavljač", login().title())
    }

    @Test
    fun customerLoginStartsEmptyWithoutRememberedCredentials() {
        start(PreLoginConfig.Login(KarikaType.SHOP))

        assertEquals("", login().email.value)
        assertEquals("", login().pass.value)
        assertFalse(login().rememberMe.value)
        assertFalse(login().formValid.value)
    }

    @Test
    fun customerLoginIsPrefilledFromRememberedCredentials() {
        persistence.save("user_usernameSHOP", "amar@karika.ba")
        persistence.save("user_passwordSHOP", "Lozinka1")
        start(PreLoginConfig.Login(KarikaType.SHOP))

        assertEquals("amar@karika.ba", login().email.value)
        assertEquals("Lozinka1", login().pass.value)
        assertTrue(login().rememberMe.value)
        assertTrue(login().formValid.value)
    }

    @Test
    fun forgotPasswordTogglesTheSheet() {
        start(PreLoginConfig.Login(KarikaType.SHOP))

        login().forgotPassword()
        assertTrue(login().forgotPassSheet.value)

        login().forgotPassword()
        assertFalse(login().forgotPassSheet.value)
    }

    @Test
    fun customerLoginOpensCustomerRegistrationAndBack() {
        start()
        assertIs<PreLoginChild.Landing>(active).component.navigateLogin(KarikaType.SHOP)

        login().navigateRegistration()
        assertEquals(KarikaType.SHOP, registration().userType)
        assertEquals("Registracija kupca", registration().title)

        registration().navigateBack()
        assertTrue(login().isShop())
    }

    @Test
    fun vendorLoginOpensVendorRegistration() {
        start(PreLoginConfig.Login(KarikaType.VENDOR))

        login().navigateRegistration()

        assertEquals(KarikaType.VENDOR, registration().userType)
        assertEquals("Registracija dobavljača", registration().title)
    }

    @Test
    fun backFromLoginReturnsToLandingOnly() {
        start()
        assertIs<PreLoginChild.Landing>(active).component.navigateLogin(KarikaType.SHOP)

        login().navigateLanding()

        assertIs<PreLoginChild.Landing>(active)
        assertEquals(1, root.stack.value.items.size)
    }

    @Test
    fun registeringAnEmptyFormShowsTheFirstValidationMessage() {
        start(PreLoginConfig.Registration(KarikaType.SHOP))

        registration().register()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(
            "Naziv pravnog lica je obavezno polje!",
            stateHolder.hostState.currentSnackbarData?.visuals?.message
        )
    }
}
