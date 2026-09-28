package karika.distribucija.ba.ui.view.prelogin

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Base for the pre-login screen tests. They run on the JVM through Robolectric, with a plain
 * Application instead of KarikaApp so Koin is not started, and a phone-sized screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
abstract class PreLoginUiTest {
    @get:Rule
    val compose = createComposeRule()

    @OptIn(ExperimentalResourceApi::class)
    @Before
    fun initComposeResources() {
        // AndroidContextProvider (a ContentProvider) never runs under Robolectric, so the
        // compose resources (icons, fonts) need the context handed to them directly.
        setResourceReaderAndroidContext(RuntimeEnvironment.getApplication())
    }
}
