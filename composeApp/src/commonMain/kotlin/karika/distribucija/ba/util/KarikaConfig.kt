package karika.distribucija.ba.util

import karika.distribucija.ba.ui.common.getEnvPrefix

class KarikaConfig {
    companion object {
        fun getOutletId(): Int = 303

        fun getActionId(): Int = 301

        fun getKarikaProductsId(): Int =
            when (getEnvPrefix()) {
                "test." -> 363
                else -> 439
            }
    }
}