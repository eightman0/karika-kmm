package karika.distribucija.ba.ui.view.shop.cart

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CartMathTest {

    private fun product(id: String, price: Double, specialPrice: Double? = null) =
        Product(entityId = id, createdAt = "2024-01-01 00:00:00", name = id, price = price, specialPrice = specialPrice)

    private fun vendor(id: Int, minimum: String? = null) = Vendor(entityId = id, minOrderAmount = minimum)

    private fun cart(vararg vendors: Pair<Vendor, List<Pair<Product, Int>>>) = mapOf(*vendors)

    private fun Map<Vendor, List<Pair<Product, Int>>>.entryOf(id: Int) = entries.first { it.key.entityId == id }

    @Test
    fun totalAddsPdvToEveryProductTimesItsQuantity() {
        val items = cart(
            vendor(1) to listOf(product("a", 10.0) to 2),
            vendor(2) to listOf(product("b", 5.0) to 1),
        )

        // (10 × 2 + 5) × 1.17
        assertEquals("29,25 KM", items.calculateTotal())
    }

    @Test
    fun totalUsesTheDiscountedPrice() {
        val items = cart(vendor(1) to listOf(product("a", 10.0, specialPrice = 8.0) to 1))

        assertEquals("9,36 KM", items.calculateTotal())
    }

    @Test
    fun emptyCartTotalsZero() {
        assertEquals("0,00 KM", cart().calculateTotal())
    }

    @Test
    fun missingAmountIsTheMinimumLessTheTotalWithPdv() {
        val items = cart(vendor(1, minimum = "100") to listOf(product("a", 50.0) to 1))

        // 100 - 50 × 1.17
        assertEquals("41,50", items.entryOf(1).minAmountRest())
        assertEquals(41.5, items.entryOf(1).minAmountRestValue().toDouble())
        assertFalse(items.orderValid())
    }

    @Test
    fun nothingIsMissingOnceTheMinimumIsPassed() {
        val items = cart(vendor(1, minimum = "50") to listOf(product("a", 50.0) to 1))

        assertEquals("0,00", items.entryOf(1).minAmountRest())
        assertTrue(items.orderValid())
    }

    @Test
    fun aFractionOfACentBelowTheMinimumDoesNotBlockTheOrder() {
        // 85,47 × 1.17 = 99,9999: shown as 100,00 KM total and 0,00 KM missing
        val items = cart(vendor(1, minimum = "100") to listOf(product("a", 85.47) to 1))

        assertEquals("100,00 KM", items.calculateTotal())
        assertEquals("0,00", items.entryOf(1).minAmountRest())
        assertTrue(items.orderValid())
    }

    @Test
    fun oneVendorBelowItsMinimumBlocksTheWholeOrder() {
        val items = cart(
            vendor(1, minimum = "50") to listOf(product("a", 50.0) to 1),
            vendor(2, minimum = "100") to listOf(product("b", 10.0) to 1),
        )

        assertFalse(items.orderValid())
    }

    @Test
    fun vendorWithoutMinimumNeverBlocks() {
        val items = cart(vendor(1) to listOf(product("a", 1.0) to 1))

        assertEquals("0,00", items.entryOf(1).minAmountRest())
        assertTrue(items.orderValid())
    }

    @Test
    fun progressIsTheShareOfTheMinimumReached() {
        val items = cart(vendor(1, minimum = "100") to listOf(product("a", 50.0) to 1))

        val (reached, missing) = items.entryOf(1).progress()

        assertEquals(0.585f, reached, 0.0001f)
        assertEquals(0.415f, missing, 0.0001f)
    }

    @Test
    fun vendorWithNothingInTheCartGetsAFullBar() {
        // current == 0 is special-cased to (1, 0), the same as a reached minimum
        val items = cart(vendor(1, minimum = "100") to emptyList())

        assertEquals(1f to 0f, items.entryOf(1).progress())
    }
}
