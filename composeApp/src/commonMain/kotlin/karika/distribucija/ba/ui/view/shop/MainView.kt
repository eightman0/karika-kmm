package karika.distribucija.ba.ui.view.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import karika.distribucija.ba.ui.components.BottomBar
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.SideBar
import karika.distribucija.ba.ui.components.TopBar
import karika.distribucija.ba.ui.components.isTabletLandscape
import karika.distribucija.ba.ui.view.shop.cart.CartView
import karika.distribucija.ba.ui.view.shop.cart.nextstep.ShippingDetailsView
import karika.distribucija.ba.ui.view.shop.cart.success.CartSuccessView
import karika.distribucija.ba.ui.view.shop.home.HomeView
import karika.distribucija.ba.ui.view.shop.menu.MenuView
import karika.distribucija.ba.ui.view.shop.menu.categories.CategoriesView
import karika.distribucija.ba.ui.view.shop.menu.categories.products.ProductByCategoryView
import karika.distribucija.ba.ui.view.shop.product.ProductView
import karika.distribucija.ba.ui.view.shop.profile.ProfileView
import karika.distribucija.ba.ui.view.shop.search.SearchView
import karika.distribucija.ba.ui.view.shop.vendor.VendorView
import karika.distribucija.ba.ui.view.shop.vendor.details.VendorDetailsView

@Composable
fun MainView(component: MainComponent) {
    if (isTabletLandscape()) {
        MainViewTablet(component)
    } else {
        MainViewPhone(component)
    }
}

@Composable
fun MainViewPhone(component: MainComponent) {
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

@Composable
fun MainViewTablet(component: MainComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(color = KarikaColors.White),
            contentAlignment = Alignment.Center
        ) {
            SideBar(component)
        }
        VerticalDivider()
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
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
}