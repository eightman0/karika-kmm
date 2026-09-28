package karika.distribucija.ba.ui.view.shop.cart.nextstep

/** Test tags for the shipping details screen. */
object ShippingTestTags {
    const val FIRSTNAME = "shipping_firstname"
    const val LASTNAME = "shipping_lastname"
    const val COMPANY = "shipping_company"
    const val CITY = "shipping_city"
    const val STREET = "shipping_street"
    const val POSTAL = "shipping_postal"
    const val PHONE = "shipping_phone"
    const val VENDOR_NOTE = "shipping_vendor_note"

    /** The card of one of the customer's addresses. */
    fun address(id: Int?) = "shipping_address_$id"
}
