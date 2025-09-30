package karika.distribucija.ba.ui.view.main.menu

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.main.MainConfig

class MenuComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {

    fun categories() {
        mainNavigate(MainConfig.Categories)
    }

    fun vendors() {

    }

    fun blog() {
        appNavigate(AppConfig.Blogs)
    }

    fun karika() {
        mainNavigate(
            MainConfig.CategoryProducts(
                Category(
                    id = 319,
                    name = "Samo na Kariki"
                )
            )
        )
    }
}