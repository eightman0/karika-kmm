package karika.distribucija.ba.ui.view.shop.cart

import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.util.karikaPriceFormat
import kotlin.math.round

/*
 * The cart's money rules, on the cart as CartHandler groups it: products (with their
 * quantity) per vendor. Prices are without PDV; 17 % PDV is added on top.
 */

/** The whole cart with PDV, formatted, e.g. "117,00 KM". */
fun Map<Vendor, List<Pair<Product, Int>>>.calculateTotal(): String {
    val total = values
        .flatten()
        .sumOf { (product, quantity) -> product.currentPrice() * quantity }

    return karikaPriceFormat(total * 1.17) + " KM"
}

/**
 * How much (with PDV) is still missing to the vendor's minimum order, in whole cents: a
 * total of 99,9999 KM against a 100 KM minimum is shown as 0,00 KM missing, so it must not
 * block the order either.
 */
private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.missingToMinimum(): Double {
    val minimum = key.minOrderAmount()?.toDoubleOrNull() ?: 0.0
    val current = value.sumOf { it.first.currentPrice() * it.second } * 1.17
    return (round((minimum - current) * 100) / 100).coerceAtLeast(0.0)
}

/** How much (with PDV) is still missing to the vendor's minimum order, unformatted. */
fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRestValue(): String {
    return "${missingToMinimum()}"
}

/** How much (with PDV) is still missing to the vendor's minimum order, formatted. */
fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRest(): String {
    return karikaPriceFormat(missingToMinimum())
}

/** The filled and the missing share of the vendor's minimum order, for the progress bar. */
fun Map.Entry<Vendor, List<Pair<Product, Int>>>.progress(): Pair<Float, Float> {
    val min = key.minOrderAmount()?.toDoubleOrNull() ?: 0.0
    val current = (value.sumOf { it.first.currentPrice() * it.second } * 1.17).coerceAtLeast(0.0)
    if (current == 0.0) {
        return Pair(1f, 0f)
    }

    return Pair(
        (current / min).toFloat(),
        1f - (current / min).toFloat()
    )
}

/** Whether every vendor's minimum order is reached. */
fun Map<Vendor, List<Pair<Product, Int>>>.orderValid(): Boolean {
    return all {
        it.minAmountRestValue().toDouble() == 0.0
    }
}
