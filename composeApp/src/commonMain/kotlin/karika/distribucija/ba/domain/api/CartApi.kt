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
import karika.distribucija.ba.domain.model.PaymentMethod
import karika.distribucija.ba.domain.model.PlaceOrder
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SetShippingAddressRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(true))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun placeOrder(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .placeOrder()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.bodyAsText()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun addToCart(item: AddToCart): Flow<ResultState<CartItem>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .addToCart(item)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun updateCart(item: AddToCart): Flow<ResultState<CartItem>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .updateCart(item)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun removeFromCart(itemId: String): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .removeFromCart(itemId)
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun createCart(): Flow<ResultState<String>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .createCart()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }

    fun getCart(): Flow<ResultState<Cart>> = flow {
        emit(ResultState.Loading)
        try {
            val response = CartApi()
                .getCart()
                .getOrNull()
            if (response != null && response.status == HttpStatusCode.OK) {
                emit(ResultState.Success(response.body()))
            } else {
                emit(
                    ResultState.Error("error")
                )
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e.message))
        }
    }
}