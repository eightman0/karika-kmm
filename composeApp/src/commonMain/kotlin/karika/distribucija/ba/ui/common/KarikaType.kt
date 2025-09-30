package karika.distribucija.ba.ui.common

enum class KarikaType {
    SHOP, VENDOR;

    fun isShop() = this == SHOP
}