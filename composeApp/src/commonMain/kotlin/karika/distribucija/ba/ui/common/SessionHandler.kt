package karika.distribucija.ba.ui.common

import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class SessionHandler : KoinComponent {
    private val persistenceManager: PersistenceManager = get()
    var accessToken = mutableStateOf(persistenceManager.get("JWT_TOKEN"))
    private val userRepository = UserRepository()
    private val _userDetails = MutableStateFlow(UserDetails())
    val userDetails = _userDetails.asStateFlow()
    private val _config = MutableStateFlow(Config())
    val config = _config.asStateFlow()

    init {
        HttpClientProvider.token = accessToken.value
        if (accessToken.value.isNotEmpty()) {
            getUserDetails()
            getConfig()
        }
    }

    fun saveJWT(jwt: String, loginDto: LoginDto, rememberMe: Boolean) {
        persistenceManager.save("JWT_TOKEN", jwt)
        if (rememberMe) {
            rememberLogin(loginDto)
        }
        getUserDetails()
    }

    private fun rememberLogin(loginDto: LoginDto) {
        persistenceManager.save("user_username", loginDto.username)
        persistenceManager.save("user_password", loginDto.password)
        persistenceManager.save("user_type", loginDto.userType)
    }

    fun getUserUsername() = persistenceManager.get("user_username")
    fun getUserPassword() = persistenceManager.get("user_password")
    fun hasJWT() = accessToken.value.isNotEmpty()
    open fun logout() {
        persistenceManager.save("JWT_TOKEN", "")
    }

    fun getUserDetails(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.get()
                .collect { result ->
                    callback.invoke()
                    if (result is ResultState.Success) {
                        _userDetails.update { result.data }
                    }
                }
        }
    }

    private fun getConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.config()
                .collect { result ->
                    if (result is ResultState.Success) {
                        _config.update { result.data }
                    }
                }
        }
    }
}