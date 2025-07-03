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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import karika.distribucija.ba.Screen
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_navigation_cart
import karikav2.composeapp.generated.resources.ic_navigation_category
import karikav2.composeapp.generated.resources.ic_navigation_home
import karikav2.composeapp.generated.resources.ic_navigation_menu
import karikav2.composeapp.generated.resources.ic_navigation_profile
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BottomBar(navController: NavController) {
    Column {
        NavigationBar(
            modifier = Modifier
                .shadow(12.dp),
            containerColor = KarikaColors.White
        ) {
            NavigationButtons(navController) { isSelected, selectedIcon, unselectedIcon, text, onClick ->
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
                                            text = "1",
                                            color = KarikaColors.White,
                                            fontWeight = FontWeight.W700,
                                            textSize = 12.sp
                                        )
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
    navController: NavController,
    content: @Composable T.(
        isSelected: Boolean,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector,
        text: String,
        onClick: () -> Unit,
    ) -> Unit,
) {
    val state = navController.currentBackStackEntryAsState()

    content(
        state.value?.destination?.route == Screen.Home.route,
        vectorResource(Res.drawable.ic_navigation_home),
        vectorResource(Res.drawable.ic_navigation_home),
        "Početna"
    ) { navController.navigate(Screen.Home.route) }
    content(
        state.value?.destination?.route == Screen.Vendor.route,
        vectorResource(Res.drawable.ic_navigation_category),
        vectorResource(Res.drawable.ic_navigation_category),
        "Dobavljači"
    ) { navController.navigate(Screen.Vendor.route) }
    content(
        state.value?.destination?.route == Screen.Menu.route,
        vectorResource(Res.drawable.ic_navigation_menu),
        vectorResource(Res.drawable.ic_navigation_menu),
        "Meni"
    ) { navController.navigate(Screen.Menu.route) }
    content(
        state.value?.destination?.route == Screen.Cart.route,
        vectorResource(Res.drawable.ic_navigation_cart),
        vectorResource(Res.drawable.ic_navigation_cart),
        "Korpa"
    ) { navController.navigate(Screen.Cart.route) }
    content(
        state.value?.destination?.route == Screen.Profile.route,
        vectorResource(Res.drawable.ic_navigation_profile),
        vectorResource(Res.drawable.ic_navigation_profile),
        "Profil"
    ) { navController.navigate(Screen.Profile.route) }
}