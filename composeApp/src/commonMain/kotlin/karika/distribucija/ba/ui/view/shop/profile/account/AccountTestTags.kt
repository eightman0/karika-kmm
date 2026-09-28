package karika.distribucija.ba.ui.view.shop.profile.account

/** Test tags for the account screen and the change password sheet. */
object AccountTestTags {
    const val EMAIL_NOTIFICATIONS = "account_email_notifications"
    const val VIBER_NOTIFICATIONS = "account_viber_notifications"
    const val PUSH_NOTIFICATIONS = "account_push_notifications"
    const val OLD_PASSWORD = "change_password_old"
    const val NEW_PASSWORD = "change_password_new"

    const val EDIT_PROFILE = "account_edit_profile"
    const val EDIT_BILLING = "account_edit_billing"
    const val EDIT_SHIPPING = "account_edit_shipping"
    const val FORM_FIRSTNAME = "account_form_firstname"
    const val FORM_LASTNAME = "account_form_lastname"
    const val FORM_PHONE = "account_form_phone"
    const val FORM_STREET = "account_form_street"
    const val FORM_POSTAL = "account_form_postal"

    /** "Uredi" / "Obriši" of an address in "Spisak svih unesenih adresa za dostavu". */
    fun editAddress(id: Int?) = "account_edit_address_$id"
    fun deleteAddress(id: Int?) = "account_delete_address_$id"
}
