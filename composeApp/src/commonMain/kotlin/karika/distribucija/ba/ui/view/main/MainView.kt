package karika.distribucija.ba.ui.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import karika.distribucija.ba.Child
import karika.distribucija.ba.ui.components.BottomBar
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.TopBar
import karika.distribucija.ba.ui.view.main.cart.CartView
import karika.distribucija.ba.ui.view.main.cart.nextstep.ShippingDetailsView
import karika.distribucija.ba.ui.view.main.cart.success.CartSuccessView
import karika.distribucija.ba.ui.view.main.home.HomeView
import karika.distribucija.ba.ui.view.main.menu.MenuView
import karika.distribucija.ba.ui.view.main.menu.categories.CategoriesView
import karika.distribucija.ba.ui.view.main.menu.categories.products.ProductByCategoryView
import karika.distribucija.ba.ui.view.main.product.ProductView
import karika.distribucija.ba.ui.view.main.profile.ProfileView
import karika.distribucija.ba.ui.view.main.search.SearchView
import karika.distribucija.ba.ui.view.main.vendor.VendorView
import karika.distribucija.ba.ui.view.main.vendor.details.VendorDetailsView

@Composable
fun MainView(component: MainComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = KarikaColors.White)
                .height(100.dp)
        )
        KarikaScaffold(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize(),
            topBar = { TopBar(component) },
            bottomBar = { BottomBar(component) },
            component = component
        ) { padding ->
            Children(
                modifier = Modifier
                    .padding(padding),
                stack = component.stack
            ) {
                when (val child = it.instance) {
                    is MainChild.Home -> HomeView(child.component)
                    is MainChild.Vendor -> VendorView(child.component)
                    is MainChild.Menu -> MenuView(child.component)

                    is MainChild.Cart -> CartView(child.component)
                    is MainChild.CartShippingDetails -> ShippingDetailsView(child.component)
                    is MainChild.CartSuccess -> CartSuccessView(child.component)

                    is MainChild.Profile -> ProfileView(child.component)

                    is MainChild.VendorDetails -> VendorDetailsView(child.component)
                    is MainChild.ProductDetails -> ProductView(child.component)
                    is MainChild.Search -> SearchView(child.component)
                    is MainChild.Categories -> CategoriesView(child.component)
                    is MainChild.CategoryProducts -> ProductByCategoryView(child.component)
                }
            }
        }
    }
}