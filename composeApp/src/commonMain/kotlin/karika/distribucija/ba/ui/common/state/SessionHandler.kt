package karika.distribucija.ba.ui.common.state

import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.getEnvJwt
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class SessionHandler : KoinComponent {
    private val persistenceManager: PersistenceManager = get()

    fun setAccessToken(accessToken: String) {
        HttpClientProvider.token = accessToken
    }

    fun saveJWT(jwt: String, loginDto: LoginDto, rememberMe: Boolean) {
        persistenceManager.save("JWT_TOKEN", jwt)
        if (rememberMe) {
            rememberLogin(loginDto)
        }
    }

    private fun rememberLogin(loginDto: LoginDto) {
        persistenceManager.save(
            "user_username".plus(loginDto.userType.name),
            loginDto.username
        )
        persistenceManager.save(
            "user_password".plus(loginDto.userType.name),
            loginDto.password
        )
        persistenceManager.save("user_type", loginDto.userType.name)
    }

    fun getUserUsername(userType: KarikaType) =
        persistenceManager.get("user_username".plus(userType.name))

    fun getUserPassword(userType: KarikaType) =
        persistenceManager.get("user_password".plus(userType.name))


    fun logout() {
        persistenceManager.save("JWT_TOKEN", "")
    }

    fun delete() {
        val type = persistenceManager.get("user_type")
        persistenceManager.save("JWT_TOKEN", "")
        persistenceManager.save("user_type", "")
        persistenceManager.save("user_username".plus(type), "")
        persistenceManager.save("user_password".plus(type), "")
    }

    fun hasJWT() = persistenceManager.get("JWT_TOKEN").isNotEmpty()

    fun userType() = KarikaType.valueOf(persistenceManager.get("user_type")).toUserType()
    fun mainConfig(): AppConfig {
        val type = persistenceManager.get("user_type")
        val jwt = persistenceManager.get("JWT_TOKEN")

        if (type.isEmpty() || jwt.isEmpty() || jwt == getEnvJwt()) {
            return AppConfig.PreLogin(
                if (isKiosk()) PreLoginConfig.Login(KarikaType.SHOP) else PreLoginConfig.Landing
            )
        }

        HttpClientProvider.token = jwt.ifEmpty { getEnvJwt() }

        return if (type == KarikaType.VENDOR.name)
            AppConfig.Dashboard else AppConfig.Main
    }
}