package karika.distribucija.ba.ui.common

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor

/**
 * What a product or vendor card (ProductItem, VendorName, VendorItem, ProductQtyAction) can do. CommonComponent implements it, so
 * every screen keeps passing itself, and a UI test can pass a fake instead.
 */
interface ProductActions {
    fun navigateToProduct(product: Product)

    fun addToCart(product: Product, qty: Int = 1, showSnack: Boolean = true)

    /** Sets the product's quantity in the cart (adds it when it is not there yet). */
    fun updateCart(product: Product, qty: Int = 1, errorCallback: () -> Unit = {})

    fun showVendor(vendor: Vendor)

    fun getUnit(unit: String): String

    fun isGuest(): Boolean
}
