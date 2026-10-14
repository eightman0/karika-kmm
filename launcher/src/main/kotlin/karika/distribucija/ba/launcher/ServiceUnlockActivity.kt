package karika.distribucija.ba.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import karika.distribucija.ba.logging.AppLogger
import java.security.MessageDigest

/**
 * On-device service access for a technician when the server side is no help (push broken, no
 * network, payload stuck): the hidden corner gesture - in the kiosk payload app or here in the
 * launcher - opens this screen, and the service PIN takes the device out of lock task for
 * RemoteDebugUnlock's 15 minutes (it re-locks on its own after that, or on "Vrati kiosk").
 *
 * Exported so the payload app can start it; the PIN is the only gate, checked offline against the
 * salted hash compiled in from launcher/keystore.properties (see build.gradle.kts). Wrong PINs are
 * rate limited, and every attempt lands in the launcher's remote log.
 */
class ServiceUnlockActivity : AppCompatActivity() {

    private lateinit var pinField: EditText
    private lateinit var message: TextView
    private lateinit var unlockButton: Button
    private lateinit var unlockedPanel: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "Service unlock screen opened by ${callingPackage ?: referrer}")
        setContentView(buildContent())
        render()
    }

    private fun render() {
        val unlocked = RemoteDebugUnlock.isActive(this)
        unlockedPanel.visibility = if (unlocked) View.VISIBLE else View.GONE
        pinField.visibility = if (unlocked) View.GONE else View.VISIBLE
        unlockButton.visibility = if (unlocked) View.GONE else View.VISIBLE
        message.text = when {
            unlocked -> "Kiosk je otključan na ${RemoteDebugUnlock.DURATION_MINUTES} min, nakon toga se vraća sam."
            BuildConfig.SERVICE_PIN_SHA256.isEmpty() -> "Servisni PIN nije postavljen u ovom buildu launchera."
            lockedOutMinutes() > 0 -> "Previše pogrešnih pokušaja. Pokušaj ponovo za ${lockedOutMinutes()} min."
            else -> "Unesi servisni PIN."
        }
    }

    private fun tryUnlock() {
        if (BuildConfig.SERVICE_PIN_SHA256.isEmpty() || lockedOutMinutes() > 0) {
            render()
            return
        }
        val pin = pinField.text.toString().trim()
        pinField.setText("")
        if (hash(pin) == BuildConfig.SERVICE_PIN_SHA256) {
            AppLogger.w(TAG, "Service unlock: correct PIN, lock task off for ${RemoteDebugUnlock.DURATION_MINUTES} min")
            prefs().edit().putInt(KEY_FAILED, 0).apply()
            KioskUnlock.unlock(this)
        } else {
            val failed = prefs().getInt(KEY_FAILED, 0) + 1
            AppLogger.w(TAG, "Service unlock: wrong PIN ($failed)")
            val edit = prefs().edit().putInt(KEY_FAILED, failed)
            if (failed >= MAX_ATTEMPTS) {
                edit.putLong(KEY_LOCKED_UNTIL, System.currentTimeMillis() + LOCKOUT_MS).putInt(KEY_FAILED, 0)
            }
            edit.apply()
            message.text = "Pogrešan PIN."
            if (failed >= MAX_ATTEMPTS) render()
            return
        }
        render()
    }

    private fun lockedOutMinutes(): Long {
        val remaining = prefs().getLong(KEY_LOCKED_UNTIL, 0L) - System.currentTimeMillis()
        return if (remaining > 0) remaining / 60_000 + 1 else 0
    }

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @SuppressLint("SetTextI18n")
    private fun buildContent(): View {
        fun dp(value: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
        fun button(text: String, onClick: () -> Unit) = Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(32), dp(48), dp(32), dp(32))
        }
        root.addView(TextView(this).apply {
            text = "Servisni pristup"
            setTextColor(Color.parseColor("#1A1F36"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        message = TextView(this).apply {
            setTextColor(Color.parseColor("#6B7280"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(20))
        }
        root.addView(message)
        pinField = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            hint = "PIN"
        }
        root.addView(pinField, LinearLayout.LayoutParams(dp(260), LinearLayout.LayoutParams.WRAP_CONTENT))
        unlockButton = button("Otključaj") { tryUnlock() }
        root.addView(unlockButton, LinearLayout.LayoutParams(dp(260), LinearLayout.LayoutParams.WRAP_CONTENT))

        unlockedPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(button("Otvori postavke") {
                startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
            addView(button("Vrati kiosk") {
                AppLogger.i(TAG, "Service unlock: relocked by technician")
                KioskUnlock.relock(this@ServiceUnlockActivity)
                finish()
            })
        }
        root.addView(unlockedPanel, LinearLayout.LayoutParams(dp(260), LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(button("Zatvori") { finish() }, LinearLayout.LayoutParams(dp(260), LinearLayout.LayoutParams.WRAP_CONTENT))
        return root
    }

    companion object {
        private const val TAG = "ServiceUnlock"
        private const val PREFS = "service_unlock"
        private const val KEY_FAILED = "failed_attempts"
        private const val KEY_LOCKED_UNTIL = "locked_until"
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_MS = 5 * 60_000L

        /** Same salt as servicePinSha256() in build.gradle.kts. */
        fun hash(pin: String): String = MessageDigest.getInstance("SHA-256")
            .digest("karika-kiosk-service:$pin".toByteArray())
            .joinToString("") { "%02x".format(it) }

        fun open(context: Context) {
            context.startActivity(
                Intent(context, ServiceUnlockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
