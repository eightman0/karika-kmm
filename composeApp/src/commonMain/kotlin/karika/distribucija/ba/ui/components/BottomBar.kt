package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.ui.view.shop.MainChild
import karika.distribucija.ba.ui.view.shop.MainComponent
import karika.distribucija.ba.ui.view.shop.MainConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_home
import karikav2.composeapp.generated.resources.ic_k_menu
import karikav2.composeapp.generated.resources.ic_k_store
import karikav2.composeapp.generated.resources.ic_k_user
import org.jetbrains.compose.resources.vectorResource

/** Test tag of a bottom navigation item, for the end-to-end tests. */
fun bottomNavTag(text: String) = "bottom_nav_$text"

/** Floating dark navigation bar of the customer app. */
@Composable
fun BottomBar(
    component: MainComponent
) {
    val cart by component.stateHolder.cartHandler.cart.collectAsState()
    val totalCartItems = cart.items.values.sumOf { it.size }
    val cartIcon = vectorResource(Res.drawable.ic_k_cart)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KarikaUiColors.Page)
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(KarikaUiColors.Navy)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButtons(component) { isSelected, icon, _, text, onClick ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag(bottomNavTag(text))
                        .clickable(onClick = onClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = icon,
                            contentDescription = text,
                            tint = if (isSelected) KarikaColors.White else KarikaColors.White.copy(alpha = 0.6f)
                        )
                        if (icon == cartIcon && totalCartItems > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 10.dp, y = (-8).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(KarikaUiColors.Pink),
                                contentAlignment = Alignment.Center
                            ) {
                                KarikaText(
                                    text = "$totalCartItems",
                                    color = KarikaColors.White,
                                    fontWeight = FontWeight.W700,
                                    textSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    KarikaText(
                        text = text,
                        color = if (isSelected) KarikaColors.White else KarikaColors.White.copy(alpha = 0.6f),
                        fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500,
                        maxLines = 1,
                        textSize = 11.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) KarikaUiColors.Pink else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun SideBar(
    component: MainComponent
) {
    val cart by component.stateHolder.cartHandler.cart.collectAsState()
    SideBar(
        modifier = Modifier,
        containerColor = KarikaColors.White
    ) {
        NavigationButtons(component) { isSelected, selectedIcon, unselectedIcon, text, onClick ->
            Row {
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
                            // show badge only when there are any items in cart (sum of sizes of value collections)
                            val totalCartItems = cart.items.values.sumOf { it.size }
                            if (totalCartItems > 0) {
                                if (selectedIcon == vectorResource(Res.drawable.ic_k_cart)) {
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
                                                text = "${totalCartItems}",
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
                            maxLines = 1,
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

    if (component.isGuest()) {
        content(
            activeChild is MainChild.Home,
            vectorResource(Res.drawable.ic_k_home),
            vectorResource(Res.drawable.ic_k_home),
            "Početna"
        ) {
            component.navigate(MainConfig.Home)
        }
        content(
            activeChild is MainChild.Cart,
            vectorResource(Res.drawable.ic_k_cart),
            vectorResource(Res.drawable.ic_k_cart),
            "Korpa"
        ) {
            component.reloadCart()
            component.navigate(MainConfig.Cart)
        }
        content(
            activeChild is MainChild.Menu,
            vectorResource(Res.drawable.ic_k_menu),
            vectorResource(Res.drawable.ic_k_menu),
            "Meni"
        ) {
            component.navigate(MainConfig.Menu)
        }
        content(
            activeChild is MainChild.Profile,
            vectorResource(Res.drawable.ic_k_user),
            vectorResource(Res.drawable.ic_k_user),
            "Profil"
        ) {
            component.navigate(MainConfig.Profile)
        }
        return
    }

    content(
        activeChild is MainChild.Home,
        vectorResource(Res.drawable.ic_k_home),
        vectorResource(Res.drawable.ic_k_home),
        "Početna"
    ) {
        component.navigate(MainConfig.Home)
    }
    content(
        activeChild is MainChild.Vendor,
        vectorResource(Res.drawable.ic_k_store),
        vectorResource(Res.drawable.ic_k_store),
        "Dobavljači"
    ) {
        component.navigate(MainConfig.Vendor)
    }
    content(
        activeChild is MainChild.Menu,
        vectorResource(Res.drawable.ic_k_menu),
        vectorResource(Res.drawable.ic_k_menu),
        "Meni"
    ) {
        component.navigate(MainConfig.Menu)
    }
    content(
        activeChild is MainChild.Cart,
        vectorResource(Res.drawable.ic_k_cart),
        vectorResource(Res.drawable.ic_k_cart),
        "Korpa"
    ) {
        component.reloadCart()
        component.navigate(MainConfig.Cart)
    }
    content(
        activeChild is MainChild.Profile,
        vectorResource(Res.drawable.ic_k_user),
        vectorResource(Res.drawable.ic_k_user),
        "Profil"
    ) {
        component.navigate(MainConfig.Profile)
    }
}

@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .selectableGroup(),
            content = content
        )
    }
}