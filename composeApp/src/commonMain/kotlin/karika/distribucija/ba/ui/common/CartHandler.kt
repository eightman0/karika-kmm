package karika.distribucija.ba.ui.common

import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.Cart
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
import org.koin.core.component.KoinComponent

open class CartHandler : SessionHandler(), KoinComponent {
    private val cartRepository = CartRepository()
    var cartId: String = ""
    private val _cart = MutableStateFlow(Cart())
    val cart = _cart.asStateFlow()

    private val _cart1 = MutableStateFlow<Map<Vendor, List<Pair<Product, Int>>>>(mapOf())
    val cart1 = _cart1.asStateFlow()

    init {
        if (accessToken.value.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                cartRepository.createCart()
                    .collect { result ->
                        if (result is ResultState.Success) {
                            cartId = result.data
                        }
                    }
            }

            reloadCart()
        }
    }

    fun reloadCart() {
        CoroutineScope(Dispatchers.IO).launch {
            cartRepository.getCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        _cart.update { result.data }
                        _cart1.update {
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
            cartRepository.createCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        cartId = result.data
                        reloadCart()
                    }
                }
        }
    }
}