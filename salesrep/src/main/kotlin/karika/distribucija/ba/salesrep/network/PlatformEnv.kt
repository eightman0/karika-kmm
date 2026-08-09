package karika.distribucija.ba.salesrep.network

import karika.distribucija.ba.salesrep.BuildConfig

object PlatformEnv {
    fun envPrefix(): String = BuildConfig.ENV_PREFIX
    fun envJwt(): String = BuildConfig.ENV_JWT
    fun appVersionName(): String = "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
    fun userAgent(): String = "os:Android;version:${appVersionName()}"
}
