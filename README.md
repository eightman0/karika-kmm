### Activate Kiosk mode:
adb shell dpm set-device-owner karika.distribucija.ba.kiosk/karika.distribucija.ba.provision.KarikaDeviceAdminReceiver

### Deactivate Kiosk mode:
adb shell dpm remove-active-admin karika.distribucija.ba.kiosk/karika.distribucija.ba.provision.KarikaDeviceAdminReceiver

### QR Code provisioning
```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "karika.distribucija.ba.kiosk/karika.distribucija.ba.provision.KarikaDeviceAdminReceiver",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "vdoryWCVYyTkr4sXwnQ87szRcP0ArmLuxYCuS5qtdCc=",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://test.karika.ba/app-builds/android-kiosk.apk",
  "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": true
}
```
![qr-code-json.png](qr-code-json.png)

### Tests
UI tests (landing, login, forgot password, registration, home, search, product details, categories, cart, checkout) run on the JVM through Robolectric, no emulator needed:
```
./gradlew :composeApp:testUatDebugUnitTest
```
The views are driven by fake components (`FakeComponents.kt`, `Fake*Component.kt`); `PreLoginNavigationTest` checks the real components navigate between each other. The registration rules and the cart's money rules have plain unit tests in `src/commonTest`.

The `Live*ApiTest` classes run against the real backend of the flavor (uat → `test.karika.ba`) with an existing, approved test account. `LiveLoginApiTest` logs in and requests a password reset (a real email); The other `Live*ApiTest` classes log that customer in and drive the home, search, product details, categories, category products and cart screens. Tests that touch the cart put one product in and take it out again afterwards; the ones that empty the cart or place an order only run when the account's cart was empty to begin with. `LiveCheckoutApiTest` places a real order (with a note saying it is an automatic test) and cancels it right away. Give it the account through environment variables or `~/.gradle/gradle.properties`, never the repo:
```
KARIKA_TEST_SHOP_EMAIL=...
KARIKA_TEST_SHOP_PASSWORD=...
# optional
KARIKA_TEST_VENDOR_EMAIL=...
KARIKA_TEST_VENDOR_PASSWORD=...
```
Without them those tests are reported as skipped. To run only them:
```
./gradlew :composeApp:testUatDebugUnitTest --tests '*Live*ApiTest'
```
