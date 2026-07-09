package karika.distribucija.ba.ui.common

import karika.distribucija.ba.domain.model.UserType

enum class KarikaType {
    SHOP, VENDOR, SALES_REP;

    fun isShop() = this == SHOP

    fun toUserType() = when (this) {
        SHOP -> UserType.CUSTOMER
        VENDOR -> UserType.VENDOR
        SALES_REP -> UserType.SALES_REP
    }
}