package karika.distribucija.ba.ui.common

import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.di.PersistenceManager
import karika.distribucija.ba.domain.HttpClientProvider
import karika.distribucija.ba.domain.api.CategoryRepository
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.api.MessagesRepository
import karika.distribucija.ba.domain.api.NotificationRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.LoginDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.view.prelogin.registration.isShop
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
    private val userRepository = UserRepository()
    private val _userDetails = MutableStateFlow(UserDetails())
    val userDetails = _userDetails.asStateFlow()
    private val _config = MutableStateFlow(Config())
    val config = _config.asStateFlow()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()
    val messageUnreadCountAdmin = MutableStateFlow(0)
    val messageUnreadCountUser = MutableStateFlow(0)
    val notificationCount = MutableStateFlow(0)

    private val dashRepository = DashRepository()
    private val _vendorDetails = MutableStateFlow(Vendor())
    val vendorDetails = _vendorDetails.asStateFlow()
    var appType: AppConfig =
        if (persistenceManager.get("user_type").isShop()) AppConfig.Main else AppConfig.Dashboard

    init {
        persistenceManager.get("JWT_TOKEN")
            .takeIf { it.isNotEmpty() }
            ?.let {
                setAccessToken(it)
            }
    }

    open fun setAccessToken(accessToken: String) {
        HttpClientProvider.token = accessToken
        if (appType == AppConfig.Main) {
            getUserDetails()
        } else {
            getVendorDetails()
        }

        fetchCategories()
        notificationReceived()
        getConfig()
    }

    fun saveJWT(jwt: String, loginDto: LoginDto, rememberMe: Boolean) {
        persistenceManager.save("JWT_TOKEN", jwt)
        if (rememberMe) {
            rememberLogin(loginDto)
        }
        getUserDetails()
    }

    private fun rememberLogin(loginDto: LoginDto) {
        persistenceManager.save(
            "user_username".appendUserType(loginDto.userType),
            loginDto.username
        )
        persistenceManager.save(
            "user_password".appendUserType(loginDto.userType),
            loginDto.password
        )
        persistenceManager.save("user_type", loginDto.userType)
    }

    fun getUserUsername(userType: String) =
        persistenceManager.get("user_username".appendUserType(userType))

    fun getUserPassword(userType: String) =
        persistenceManager.get("user_password".appendUserType(userType))

    private fun hasJWT() = HttpClientProvider.token?.isNotEmpty() ?: false
    open fun logout() {
        persistenceManager.save("JWT_TOKEN", "")
    }

    fun appConfig(): AppConfig {
        return if (hasJWT()) {
            if (persistenceManager.get("user_type").isShop()) {
                appType = AppConfig.Main
                AppConfig.Main
            } else {
                appType = AppConfig.Dashboard
                AppConfig.Dashboard
            }
        } else {
            AppConfig.PreLogin
        }
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

    fun fetchCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            CategoryRepository().get()
                .collect { result ->
                    if (result is ResultState.Success) {
                        _categories.update { result.data.childrenData }
                    }
                }
        }
    }

    fun notificationReceived() {
        CoroutineScope(Dispatchers.IO).launch {
            MessagesRepository()
                .messageUnreadCount()
                .collect {
                    if (it is ResultState.Success) {
                        messageUnreadCountAdmin.value = it.data.admin()
                        messageUnreadCountUser.value = it.data.user()
                    }
                }

            if (appType == AppConfig.Main) {
                NotificationRepository()
                    .get()
                    .collect {
                        if (it is ResultState.Success) {
                            notificationCount.value = it.data.count { it1 -> it1.isRead == "true" }
                        }
                    }
            } else {
                DashRepository()
                    .notifications()
                    .collect {
                        if (it is ResultState.Success) {
                            notificationCount.value = it.data.count { it1 -> it1.isRead == "0" }
                        }
                    }
            }
        }
    }

    fun getVendorDetails(callback: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            dashRepository.getProfile()
                .collect { result ->
                    callback.invoke()
                    if (result is ResultState.Success) {
                        _vendorDetails.update { result.data }
                    }
                }
        }
    }
}

private fun String.appendUserType(userType: String): String =
    if (userType.isShop()) this else this.plus("_$userType")