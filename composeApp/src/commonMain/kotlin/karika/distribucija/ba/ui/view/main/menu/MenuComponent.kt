package karika.distribucija.ba.ui.view.main.menu

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder

class MenuComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    fun categories() {
        appNavigate(AppConfig.Categories)
    }

    fun vendors() {

    }

    fun blog() {
        appNavigate(AppConfig.Blogs)
    }

    fun karika() {
        appNavigate(
            AppConfig.CategoryProducts(
                Category(
                    id = 319,
                    name = "Samo na Kariki"
                )
            )
        )
    }
}