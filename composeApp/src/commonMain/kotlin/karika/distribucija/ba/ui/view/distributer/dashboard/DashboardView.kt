package karika.distribucija.ba.ui.view.distributer.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarDashboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.board.BoardView
import karika.distribucija.ba.ui.view.distributer.messages.admin.AdminMessagesView
import karika.distribucija.ba.ui.view.distributer.messages.customer.CustomerMessagesView
import karika.distribucija.ba.ui.view.distributer.messages.details.MessagesOverviewView
import karika.distribucija.ba.ui.view.distributer.notifications.NotificationsView
import karika.distribucija.ba.ui.view.distributer.orders.OrdersView
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsView
import karika.distribucija.ba.ui.view.distributer.products.ProductsView
import karika.distribucija.ba.ui.view.distributer.products.details.ProductDetailsView
import karika.distribucija.ba.ui.view.distributer.profile.ProfileView
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_analytics
import karikav2.composeapp.generated.resources.ic_inventory
import karikav2.composeapp.generated.resources.ic_logout
import karikav2.composeapp.generated.resources.ic_messages
import karikav2.composeapp.generated.resources.ic_navigation_profile
import karikav2.composeapp.generated.resources.ic_shopping_cart
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun DashboardView(component: DashboardComponent) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val profile = component.stateHolder.vendorSpecificHandler.vendorDetails.collectAsState()
    val navState = component.stack.subscribeAsState()

    BoxWithConstraints(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        ModalNavigationDrawer(
            modifier = Modifier
                .systemBarsPadding(),
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(maxWidth * 0.7f),
                    drawerContainerColor = KarikaColors.White,
                    drawerShape = RectangleShape
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            KarikaText(
                                modifier = Modifier,
                                text = profile.value.publicName,
                                color = KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W700,
                            )
                            KarikaText(
                                modifier = Modifier,
                                text = profile.value.email,
                                color = KarikaColors.Gray2,
                                textSize = 12.sp,
                                fontWeight = FontWeight.W400,
                            )
                        }
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .onClick {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                            imageVector = vectorResource(Res.drawable.ic_tertiary),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                    }
                    HorizontalDivider()
                    NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_analytics),
                                iconColor = if (navState.value.active.instance is DashChild.ControlBoard) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.ControlBoard) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Kontrolna ploča",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.ControlBoard,
                        onClick = {
                            component.dashNavigate(DashConfig.ControlBoard, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_shopping_cart),
                                iconColor = if (navState.value.active.instance is DashChild.Orders) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.Orders) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Upravljanje narudžbama",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.Orders,
                        onClick = {
                            component.dashNavigate(DashConfig.Orders, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                   /* NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_inventory),
                                iconColor = if (navState.value.active.instance is DashChild.Products) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.Products) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Upravljanje artiklima",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.Products,
                        onClick = {
                            component.dashNavigate(DashConfig.Products, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )*/
                    NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_messages),
                                iconColor = if (navState.value.active.instance is DashChild.CustomerMessages) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.CustomerMessages) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Poruke kupaca",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.CustomerMessages,
                        onClick = {
                            component.dashNavigate(DashConfig.CustomerMessages, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_messages),
                                iconColor = if (navState.value.active.instance is DashChild.AdminMessages) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.AdminMessages) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Poruke admina",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.AdminMessages,
                        onClick = {
                            component.dashNavigate(DashConfig.AdminMessages, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    NavigationDrawerItem(
                        modifier = Modifier,
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = KarikaColors.White,
                            selectedContainerColor = KarikaColors.Blue
                        ),
                        shape = RectangleShape,
                        label = {
                            IconTextItem(
                                modifier = Modifier,
                                icon = vectorResource(Res.drawable.ic_navigation_profile),
                                iconColor = if (navState.value.active.instance is DashChild.Profile) KarikaColors.White else KarikaColors.Gray2,
                                textColor = if (navState.value.active.instance is DashChild.Profile) KarikaColors.White else KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                text = "Korisnički profil",
                                textAlign = TextAlign.Start
                            )
                        },
                        selected = navState.value.active.instance is DashChild.Profile,
                        onClick = {
                            component.dashNavigate(DashConfig.Profile, true)
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                    HorizontalDivider()
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        NavigationDrawerItem(
                            modifier = Modifier,
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = KarikaColors.Gray20,
                            ),
                            shape = RectangleShape,
                            label = {
                                IconTextItem(
                                    icon = vectorResource(Res.drawable.ic_logout),
                                    iconColor = KarikaColors.Gray2,
                                    textColor = KarikaColors.Gray2,
                                    textSize = 16.sp,
                                    fontWeight = FontWeight.W600,
                                    text = "Odjavi se",
                                    textAlign = TextAlign.Start
                                )
                            },
                            selected = false,
                            onClick = {
                                component.logout()
                            }
                        )
                    }
                }
            },
        ) {
            KarikaScaffold(
                containerColor = KarikaColors.White,
                contentWindowInsets = WindowInsets.systemBars,
                topBar = {
                    TopBarDashboard(
                        component = component,
                        menu = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        action = {
                            component.dashNavigate(DashConfig.Notifications)
                        }
                    )
                },
                component = component
            ) {

                Children(stack = component.stack) {
                    when (val child = it.instance) {
                        is DashChild.ControlBoard -> BoardView(child.component)

                        is DashChild.Orders -> OrdersView(child.component)
                        is DashChild.OrderDetails -> OrderDetailsView(child.component)

                        is DashChild.Products -> ProductsView(child.component)
                        is DashChild.ProductDetails -> ProductDetailsView(child.component)

                        is DashChild.CustomerMessages -> CustomerMessagesView(child.component)
                        is DashChild.AdminMessages -> AdminMessagesView(child.component)
                        is DashChild.MessageDetails -> MessagesOverviewView(child.component)

                        is DashChild.Profile -> ProfileView(child.component)

                        is DashChild.Notifications -> NotificationsView(child.component)
                    }
                }
            }
        }
    }
}