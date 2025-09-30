package karika.distribucija.ba.ui.common.state.customer

import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartHandler {
    private val _cart = MutableStateFlow<Map<Vendor, List<Pair<Product, Int>>>>(mapOf())
    val cart = _cart.asStateFlow()
    var cartId: String = ""

    fun reloadCart() {
        CoroutineScope(Dispatchers.IO).launch {
            CartRepository().getCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        cartId = result.data.id.toString()
                        _cart.update {
                            result.data.items.groupBy {
                                Vendor(
                                    entityId = it.extensionAttributes?.vendorId?.toIntOrNull() ?: 0,
                                    minOrderAmount = result.data.extensionAttributes?.vendors?.find { it1 ->
                                        it1.id == (it.extensionAttributes?.vendorId?.toIntOrNull()
                                            ?: 0)
                                    }?.minOrderAmount(),
                                    publicName = result.data.extensionAttributes?.vendors?.find { it1 ->
                                        it1.id == (it.extensionAttributes?.vendorId?.toIntOrNull()
                                            ?: 0)
                                    }?.publicName,
                                )
                            }.mapValues { entry ->
                                entry.value.map {
                                    Pair(
                                        Product(
                                            name = it.name,
                                            sku = it.sku,
                                            itemId = it.itemId,
                                            price = it.price,
                                            minQtyUnit = it.extensionAttributes?.productUnit,
                                            image = it.extensionAttributes?.imageUrl,
                                            specialPrice = it.extensionAttributes?.specialPrice?.toDoubleOrNull()
                                        ),
                                        it.qty
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    fun placedOrder() {
        CoroutineScope(Dispatchers.IO).launch {
            CartRepository().createCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        reloadCart()
                    }
                }
        }
    }
}