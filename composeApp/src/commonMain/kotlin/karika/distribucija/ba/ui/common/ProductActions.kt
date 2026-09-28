package karika.distribucija.ba.ui.common

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor

/**
 * What a product card (ProductItem, VendorName) can do. CommonComponent implements it, so
 * every screen keeps passing itself, and a UI test can pass a fake instead.
 */
interface ProductActions {
    fun navigateToProduct(product: Product)

    fun addToCart(product: Product, qty: Int = 1, showSnack: Boolean = true)

    fun showVendor(vendor: Vendor)

    fun getUnit(unit: String): String

    fun isGuest(): Boolean
}
