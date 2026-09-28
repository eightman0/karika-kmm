package karika.distribucija.ba.testutil

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.StockData

/** A product as the API would return it, with only the fields a product card reads. */
fun testProduct(
    id: String,
    name: String,
    price: Double,
    specialPrice: Double? = null,
    bonus: Double? = null,
    inStock: Boolean = true,
    isNew: Boolean = false,
    minQty: Int = 1,
    vendorName: String = "Test dobavljač",
    mpc: Double? = null,
    description: String? = null,
) = Product(
    entityId = id,
    createdAt = "2024-01-01 00:00:00",
    sku = "SKU-$id",
    name = name,
    price = price,
    specialPrice = specialPrice,
    rewardPoints = bonus,
    minQty = minQty.toString(),
    stockData = StockData(isInStock = if (inStock) "1" else "0"),
    newsFromDate = if (isNew) "2000-01-01 00:00:00" else null,
    newsToDate = if (isNew) "2100-01-01 00:00:00" else null,
    vendorName = vendorName,
    vendorId = "7",
    mpc = mpc,
    description = description,
    shortDescription = description,
)
