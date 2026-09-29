package karika.distribucija.ba.ui.view.distributer.analytics

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder

class AnalyticsFiltersComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    private val onBack: () -> Unit,
) : CommonComponent(componentContext, stateHolder) {
    val filters get() = stateHolder.analyticsFilterState

    fun back() {
        filters.apply()
        onBack()
    }
}
