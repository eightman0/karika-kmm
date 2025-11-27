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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CartHandler {
    private val _cart = MutableStateFlow(CartData(items = emptyMap()))
    val cart = _cart.asStateFlow()
    var cartId: String = ""

    @OptIn(ExperimentalTime::class)
    fun reloadCart() {
        CoroutineScope(Dispatchers.IO).launch {
            CartRepository().getCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        cartId = result.data.id.toString()
                        _cart.update {
                            CartData(
                                items = result.data.items.groupBy {
                                    Vendor(
                                        entityId = it.extensionAttributes?.vendorId?.toIntOrNull()
                                            ?: 0,
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
                                                id = it.productId.toIntOrNull(),
                                                name = it.name,
                                                sku = it.sku,
                                                itemId = it.itemId,
                                                price = it.price,
                                                minQtyUnit = it.extensionAttributes?.productUnit,
                                                minQty = it.extensionAttributes?.minQty.toString(),
                                                image = it.extensionAttributes?.imageUrl,
                                                specialPrice = it.extensionAttributes?.specialPrice?.toDoubleOrNull(),
                                                rewardPoints = it.extensionAttributes?.rewardPoints
                                                    ?: 0.0,
                                            ),
                                            it.qty
                                        )
                                    }
                                },
                                lastUpdated = Clock.System.now().toEpochMilliseconds()
                            )
                        }
                    }
                }
        }
    }

    fun createCart(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            CartRepository().createCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        callback()
                        reloadCart()
                    }
                }
        }
    }
}