package karika.distribucija.ba.domain.api

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.HttpClientProvider.url
import karika.distribucija.ba.domain.model.AddToCart
import karika.distribucija.ba.domain.model.Cart
import karika.distribucija.ba.domain.model.CartItem
import karika.distribucija.ba.domain.model.ErrorResponse
import karika.distribucija.ba.domain.model.PaymentMethod
import karika.distribucija.ba.domain.model.PlaceOrder
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SetShippingAddressRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class CartApi {
    suspend fun setAddress(address: SetShippingAddressRequest): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("carts/mine/shipping-information")
        ) {
            setBody(address)
        }
    }

    suspend fun placeOrder(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("carts/mine/payment-information")
        ) {
            setBody(
                PlaceOrder(
                    paymentMethod = PaymentMethod("checkmo")
                )
            )
        }
    }

    suspend fun addToCart(item: AddToCart): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("carts/mine/items")
        ) {
            setBody(
                item
            )
        }
    }

    suspend fun updateCart(item: AddToCart): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.put(
            url("carts/mine/items/${item.cartItem.itemId}")
        ) {
            setBody(
                item
            )
        }
    }

    suspend fun removeFromCart(itemId: String): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.delete(
            url("carts/mine/items/$itemId")
        )
    }

    suspend fun createCart(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.post(
            url("carts/mine")
        )
    }

    suspend fun getCart(): Result<HttpResponse> = runCatching {
        return@runCatching HttpClientProvider.client.get(
            url("carts/mine")
        )
    }
}

class CartRepository internal constructor() {
    fun setAddress(address: SetShippingAddressRequest): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .setAddress(address)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun placeOrder(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .placeOrder()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText()))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun addToCart(item: AddToCart): Flow<ResultState<CartItem>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .addToCart(item)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<CartItem>() ))
            } else {
                val errorBody = response.body<ErrorResponse>()
                emit(ResultState.Error(errorBody.message))
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun updateCart(item: AddToCart): Flow<ResultState<CartItem>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .updateCart(item)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<CartItem>() ))
            } else {
                val error = response.body<ErrorResponse>()
                emit(
                    ResultState.Error(
                        if ("The requested qty is not available" == error.message)
                            "Tražena količina nije dostupna."
                        else if (error.message.startsWith("Current customer does not have an active cart."))
                            "Current customer does not have an active cart."
                        else "Došlo je do greške. Pokušajte ponovo!"
                    )
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun removeFromCart(itemId: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .removeFromCart(itemId)
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>() ))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun createCart(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .createCart()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<String>() ))
            } else {
                emit(
                    ResultState.Error("Došlo je do greške. Pokušajte ponovo!")
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)

    fun getCart(): Flow<ResultState<Cart>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .getCart()
                .getOrNoInternet()
            if (response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body<Cart>() ))
            } else {
                emit(
                    ResultState.Error(response.body<ErrorResponse>().message)
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }.flowOn(Dispatchers.Default)
}