package karika.distribucija.ba.ui.common.state.customer

import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.CartData
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.state.CommonHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CartHandler(val commonHandler: CommonHandler) {
    private val _cart = MutableStateFlow(CartData(items = emptyMap()))
    val cart = _cart.asStateFlow()
    var cartId: String = ""

    @OptIn(ExperimentalTime::class)
    fun reloadCart() {
        CoroutineScope(Dispatchers.Main).launch {
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
                                                entityId = it.productId,
                                                id = it.productId.toIntOrNull(),
                                                name = it.name,
                                                sku = it.sku,
                                                itemId = it.itemId,
                                                price = it.extensionAttributes?.originalPrice?.toDoubleOrNull(),
                                                mpc = it.extensionAttributes?.mpc?.toDoubleOrNull(),
                                                minQtyUnit = commonHandler.getUnitId(it.extensionAttributes?.productUnit ?: "kom"),
                                                minQty = it.extensionAttributes?.minQty.toString(),
                                                image = it.extensionAttributes?.imageUrl,
                                                specialPrice = it.extensionAttributes?.specialPrice?.toDoubleOrNull(),
                                                specialPriceFrom = it.extensionAttributes?.specialPriceFrom,
                                                specialPriceTo = it.extensionAttributes?.specialPriceTo,
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
                    } else if (result is ResultState.Error) {
                        if (result.message?.startsWith("No such entity with %fieldName") == true) {
                            _cart.update {
                                CartData(items = emptyMap())
                            }
                            createCart {
                                reloadCart()
                            }
                        }

                    }
                }
        }
    }

    fun createCart(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.Main).launch {
            CartRepository().createCart()
                .collect { result ->
                    if (result is ResultState.Success) {
                        callback()
                        reloadCart()
                    }
                }
        }
    }

    fun clearCart(callback: (String) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch {
            val items = cart.value.items.values.flatten()

            if (items.isEmpty()) {
                callback("Cart is already empty")
                return@launch
            }

            try {
                items.map { item ->
                    async {
                        CartRepository().removeFromCart("${item.first.itemId}")
                            .collect()
                    }
                }.awaitAll()

                reloadCart()
                callback("")
            } catch (e: Exception) {
                callback("Došlo je do greške. Pokušajte ponovo!")
            }
        }
    }
}