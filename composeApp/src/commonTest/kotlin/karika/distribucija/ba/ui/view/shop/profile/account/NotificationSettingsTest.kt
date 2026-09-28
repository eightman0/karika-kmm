package karika.distribucija.ba.ui.view.shop.profile.account

import karika.distribucija.ba.domain.model.Attributes
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationSettingsTest {

    private val saved = listOf(
        Attributes("b2b_pravno_lice", "Karika d.o.o."),
        Attributes("notification_email_enabled", "1"),
        Attributes("notification_viber_enabled", "1"),
        Attributes("notification_push_enabled", "0"),
    )

    private fun List<Attributes>.valueOf(code: String) = filter { it.attributeCode == code }.map { it.value }

    @Test
    fun replacesEachSettingOnce() {
        val updated = saved.withNotificationSettings(email = false, viber = true, push = true)

        assertEquals(listOf<String?>("0"), updated.valueOf("notification_email_enabled"))
        assertEquals(listOf<String?>("1"), updated.valueOf("notification_viber_enabled"))
        assertEquals(listOf<String?>("1"), updated.valueOf("notification_push_enabled"))
    }

    @Test
    fun keepsTheOtherAttributes() {
        val updated = saved.withNotificationSettings(email = true, viber = true, push = true)

        assertEquals(listOf<String?>("Karika d.o.o."), updated.valueOf("b2b_pravno_lice"))
        assertEquals(4, updated.size)
    }

    @Test
    fun addsTheSettingsWhenTheCustomerHasNone() {
        val updated = listOf(Attributes("b2b_pravno_lice", "Karika d.o.o."))
            .withNotificationSettings(email = true, viber = false, push = true)

        assertEquals(
            listOf("notification_email_enabled" to "1", "notification_viber_enabled" to "0", "notification_push_enabled" to "1"),
            updated.drop(1).map { it.attributeCode to it.value }
        )
    }

    @Test
    fun savingTwiceDoesNotPileUpSettings() {
        val twice = saved
            .withNotificationSettings(email = false, viber = false, push = false)
            .withNotificationSettings(email = true, viber = false, push = false)

        assertEquals(4, twice.size)
        assertEquals(listOf<String?>("1"), twice.valueOf("notification_email_enabled"))
    }
}
