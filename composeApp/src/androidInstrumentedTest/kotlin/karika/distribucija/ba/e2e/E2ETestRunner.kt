package karika.distribucija.ba.e2e

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

/**
 * The runner for the end-to-end tests. A run of every test (no tests picked, the whole module or
 * the whole e2e package, as Android Studio's "All in Module" and "All in Package" ask for) runs
 * [AllE2ETests] instead: each test once, in the order a user goes through the app. Picking a
 * class or a method runs just that, as before.
 */
class E2ETestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        val args = Bundle(arguments ?: Bundle())
        val picksTests = listOf("class", "notClass", "tests_regex", "notPackage").any { args.containsKey(it) }
        if (!picksTests && args.getString("package") in ALL_TESTS_PACKAGES) {
            args.remove("package")
            args.putString("class", AllE2ETests::class.java.name)
        }
        super.onCreate(args)
    }

    private companion object {
        val ALL_TESTS_PACKAGES = setOf(null, "karika.distribucija.ba", "karika.distribucija.ba.e2e")
    }
}
