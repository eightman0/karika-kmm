package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.ui.view.main.MainChild
import karika.distribucija.ba.ui.view.main.MainComponent
import karika.distribucija.ba.ui.view.main.MainConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_navigation_cart
import karikav2.composeapp.generated.resources.ic_navigation_home
import karikav2.composeapp.generated.resources.ic_navigation_menu
import karikav2.composeapp.generated.resources.ic_navigation_profile
import karikav2.composeapp.generated.resources.ic_navigation_vendors
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BottomBar(
    component: MainComponent
) {
    val cart by component.stateHolder.cart.collectAsState()
    Column {
        NavigationBar(
            modifier = Modifier
                .shadow(12.dp),
            containerColor = KarikaColors.White
        ) {
            NavigationButtons(component) { isSelected, selectedIcon, unselectedIcon, text, onClick ->
                NavigationBarItem(
                    selected = isSelected,
                    onClick = onClick,
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                contentDescription = text,
                            )
                           if (cart.items.isNotEmpty()) {
                               if (selectedIcon == vectorResource(Res.drawable.ic_navigation_cart)) {
                                   Box(
                                       modifier = Modifier
                                           .fillMaxSize(),
                                       contentAlignment = Alignment.TopEnd
                                   ) {
                                       Box(
                                           modifier = Modifier
                                               .background(
                                                   color = KarikaColors.Red,
                                                   shape = CircleShape
                                               )
                                               .size(16.dp),
                                           contentAlignment = Alignment.Center
                                       ) {
                                           KarikaText(
                                               text = "${cart.items.size}",
                                               color = KarikaColors.White,
                                               fontWeight = FontWeight.W700,
                                               textSize = 12.sp
                                           )
                                       }
                                   }
                               }
                           }
                        }
                    },
                    label = {
                        KarikaText(
                            text = text,
                            color = if (isSelected) KarikaColors.Primary else KarikaColors.Secondary,
                            fontWeight = FontWeight.W600,
                            textSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIndicatorColor = KarikaColors.White,
                        unselectedIconColor = KarikaColors.Secondary,
                        selectedIconColor = KarikaColors.Primary,
                        unselectedTextColor = KarikaColors.Secondary,
                        selectedTextColor = KarikaColors.Primary
                    )
                )
            }
        }
    }
}

@Composable
private fun <T> T.NavigationButtons(
    component: MainComponent,
    content: @Composable T.(
        isSelected: Boolean,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector,
        text: String,
        onClick: () -> Unit,
    ) -> Unit,
) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    content(
        activeChild is MainChild.Home,
        vectorResource(Res.drawable.ic_navigation_home),
        vectorResource(Res.drawable.ic_navigation_home),
        "Početna"
    ) {
        component.navigate(MainConfig.Home)
    }
    content(
        activeChild is MainChild.Vendor,
        vectorResource(Res.drawable.ic_navigation_vendors),
        vectorResource(Res.drawable.ic_navigation_vendors),
        "Dobavljači"
    ) {
        component.navigate(MainConfig.Vendor)
    }
    content(
        activeChild is MainChild.Menu,
        vectorResource(Res.drawable.ic_navigation_menu),
        vectorResource(Res.drawable.ic_navigation_menu),
        "Meni"
    ) {
        component.navigate(MainConfig.Menu)
    }
    content(
        activeChild is MainChild.Cart,
        vectorResource(Res.drawable.ic_navigation_cart),
        vectorResource(Res.drawable.ic_navigation_cart),
        "Korpa"
    ) {
        component.reloadCart()
        component.navigate(MainConfig.Cart)
    }
    content(
        activeChild is MainChild.Profile,
        vectorResource(Res.drawable.ic_navigation_profile),
        vectorResource(Res.drawable.ic_navigation_profile),
        "Profil"
    ) {
        component.navigate(MainConfig.Profile)
    }
}