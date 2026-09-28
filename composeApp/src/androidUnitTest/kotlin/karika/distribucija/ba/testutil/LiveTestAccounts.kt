package karika.distribucija.ba.testutil

import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.LoginRepository
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.KarikaType
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import kotlin.test.assertIs

/**
 * Test accounts for the tests that talk to the real backend of the flavor under test
 * (uat -> test.karika.ba). They come from environment variables or Gradle properties, see
 * composeApp/build.gradle.kts; never commit them. A test whose account is not configured is
 * reported as skipped.
 */
object LiveTestAccounts {
    val shopEmail: String? = System.getenv("KARIKA_TEST_SHOP_EMAIL")
    val shopPassword: String? = System.getenv("KARIKA_TEST_SHOP_PASSWORD")
    val vendorEmail: String? = System.getenv("KARIKA_TEST_VENDOR_EMAIL")
    val vendorPassword: String? = System.getenv("KARIKA_TEST_VENDOR_PASSWORD")

    /** Returns the customer account's email and password, or skips the calling test. */
    fun requireShop(): Pair<String, String> {
        assumeTrue(
            "KARIKA_TEST_SHOP_EMAIL / KARIKA_TEST_SHOP_PASSWORD not set",
            !shopEmail.isNullOrEmpty() && !shopPassword.isNullOrEmpty()
        )
        return shopEmail!! to shopPassword!!
    }

    /** Returns the vendor account's email and password, or skips the calling test. */
    fun requireVendor(): Pair<String, String> {
        assumeTrue(
            "KARIKA_TEST_VENDOR_EMAIL / KARIKA_TEST_VENDOR_PASSWORD not set",
            !vendorEmail.isNullOrEmpty() && !vendorPassword.isNullOrEmpty()
        )
        return vendorEmail!! to vendorPassword!!
    }

    /**
     * Logs the customer account in through the API (not the login screen) and makes its token
     * the one every request uses, for tests of the screens after login. Callers put the guest
     * token back afterwards: HttpClientProvider.token = getEnvJwt().
     */
    fun logInCustomer() {
        val (email, password) = requireShop()
        val result = runBlocking {
            LoginRepository().login(LoginDto(email, password, KarikaType.SHOP)).last()
        }
        val token = assertIs<ResultState.Success<*>>(result, "customer login failed: $result").data
        HttpClientProvider.token = token as String
    }
}
