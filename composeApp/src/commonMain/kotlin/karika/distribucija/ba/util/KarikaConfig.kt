package karika.distribucija.ba.util

import karika.distribucija.ba.ui.common.getEnvPrefix

class KarikaConfig {
    companion object {
        fun getOutletId(): Int =
            when (getEnvPrefix()) {
                "test." -> 303
                "demo." -> 303
                else -> 303
            }

        fun getActionId(): Int =
            when (getEnvPrefix()) {
                "test." -> 301
                "demo." -> 301
                else -> 301
            }

        fun getKarikaProductsId(): Int =
            when (getEnvPrefix()) {
                "test." -> 363
                "demo." -> 301
                else -> 439
            }
    }
}