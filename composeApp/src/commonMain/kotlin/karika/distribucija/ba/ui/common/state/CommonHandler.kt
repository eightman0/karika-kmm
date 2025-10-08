package karika.distribucija.ba.ui.common.state

import karika.distribucija.ba.domain.api.CategoryRepository
import karika.distribucija.ba.domain.api.UserRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Config
import karika.distribucija.ba.domain.model.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommonHandler {
    private val _config = MutableStateFlow(Config())
    val config = _config.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private fun getConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            UserRepository().config()
                .collect { result ->
                    if (result is ResultState.Success) {
                        _config.update { result.data }
                    }
                }
        }
    }

    private fun fetchCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            CategoryRepository().get()
                .collect { result ->
                    if (result is ResultState.Success) {
                        _categories.update { result.data.childrenData }
                    }
                }
        }
    }

    fun getUnit(id: String): String {
        return config.value.unitOptions.find { it.unit == id }?.label ?: "kom"
    }

    fun getPackageVolume(width: Double, height: Double, depth: Double, weight: Double): Double {
        val volume = maxOf((width * height * depth) / 5000, weight)
        val price =
            config.value.a2b()?.find { it.min() <= volume && it.max() >= volume }?.price()
                ?: config.value.a2b()?.lastOrNull()?.price() ?: 0.0
        return price + (price * 0.1)
    }

    fun init() {
        fetchCategories()
        getConfig()
    }
}