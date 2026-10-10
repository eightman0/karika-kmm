package karika.distribucija.ba.ui.view.distributer.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.ui.common.appVersionName
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.TopBarDashboard
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsFiltersView
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsTab
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsView
import karika.distribucija.ba.ui.view.distributer.analytics.atrisk.AnalyticsAtRiskView
import karika.distribucija.ba.ui.view.distributer.analytics.products.AnalyticsProductsView
import karika.distribucija.ba.ui.view.distributer.customers.CustomersView
import karika.distribucija.ba.ui.view.distributer.customers.editor.CustomerRuleEditorView
import karika.distribucija.ba.ui.view.distributer.employees.EmployeesView
import karika.distribucija.ba.ui.view.distributer.employees.locations.EmployeeLocationsView
import karika.distribucija.ba.ui.view.distributer.messages.admin.AdminMessagesView
import karika.distribucija.ba.ui.view.distributer.messages.customer.CustomerMessagesView
import karika.distribucija.ba.ui.view.distributer.messages.details.MessagesOverviewView
import karika.distribucija.ba.ui.view.distributer.messages.internal.InternalMessagesView
import karika.distribucija.ba.ui.view.distributer.notifications.NotificationsView
import karika.distribucija.ba.ui.view.distributer.orders.OrdersView
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsView
import karika.distribucija.ba.ui.view.distributer.products.ProductsView
import karika.distribucija.ba.ui.view.distributer.products.details.ProductDetailsView
import karika.distribucija.ba.ui.view.distributer.profile.ProfileView
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_chart
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_inbox
import karikav2.composeapp.generated.resources.ic_k_logout_left
import karikav2.composeapp.generated.resources.ic_k_shield
import karikav2.composeapp.generated.resources.ic_k_tag
import karikav2.composeapp.generated.resources.ic_k_user
import karikav2.composeapp.generated.resources.ic_k_users
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun DashboardView(component: DashboardComponent) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val profile = component.stateHolder.vendorSpecificHandler.vendorDetails.collectAsState()
    val me by component.stateHolder.salesSpecificHandler.me.collectAsState()
    val canSeeDashboard = me.capabilities.canSeeDashboard
    val canViewEmployees = me.vendorOperationsEnabled && me.capabilities.canViewEmployees
    val navState = component.stack.subscribeAsState()
    val messageState = component.stateHolder.vendorNotificationHandler.chatUnreadCount.asState()
    val activeInstance = navState.value.active.instance
    val isAnalyticsActive = activeInstance is DashChild.Analytics ||
        activeInstance is DashChild.AnalyticsProducts ||
        activeInstance is DashChild.AnalyticsAtRisk
    var analyticsExpanded by remember { mutableStateOf(false) }
    val activeAnalyticsTab = if (activeInstance is DashChild.Analytics) {
        activeInstance.component.selectedTab.collectAsState().value
    } else {
        null
    }

    BoxWithConstraints(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        ModalNavigationDrawer(
            modifier = Modifier,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(minOf(maxWidth * 0.82f, 340.dp)),
                    drawerContainerColor = KarikaColors.White,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
                    val go: (DashConfig) -> Unit = { config ->
                        component.dashNavigate(config, true)
                        closeDrawer()
                    }

                    // Supplier: initials, name and email, close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KInitials(
                            name = profile.value.publicName,
                            size = 48.dp,
                            shape = CircleShape,
                            background = VendorAccent,
                            color = KarikaColors.White,
                            textSize = 16.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = profile.value.publicName,
                                color = KarikaUiColors.Ink,
                                textSize = 16.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.W700,
                                maxLines = 1
                            )
                            KarikaText(
                                text = profile.value.email,
                                color = KarikaUiColors.Muted,
                                textSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 1
                            )
                        }
                        KCircleButton(
                            icon = vectorResource(Res.drawable.ic_k_close),
                            size = 40.dp,
                            iconSize = 18.dp,
                            onClick = closeDrawer
                        )
                    }
                    KDivider()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        DrawerItem(
                            text = "Analitika",
                            icon = vectorResource(Res.drawable.ic_k_chart),
                            selected = isAnalyticsActive,
                            trailing = {
                                KIcon(
                                    modifier = Modifier.rotate(if (analyticsExpanded) 180f else 0f),
                                    icon = vectorResource(Res.drawable.ic_k_chevron_down),
                                    tint = if (isAnalyticsActive) VendorAccent else KarikaUiColors.Muted,
                                    size = 18.dp
                                )
                            }
                        ) {
                            analyticsExpanded = !analyticsExpanded
                        }
                        if (analyticsExpanded) {
                            AnalyticsSubItem(
                                text = "Pregled",
                                selected = activeAnalyticsTab == AnalyticsTab.Overview
                            ) { go(DashConfig.Analytics(AnalyticsTab.Overview)) }
                            AnalyticsSubItem(
                                text = "Trendovi prodaje",
                                selected = activeAnalyticsTab == AnalyticsTab.Trends
                            ) { go(DashConfig.Analytics(AnalyticsTab.Trends)) }
                            if (canSeeDashboard) {
                                AnalyticsSubItem(
                                    text = "Komercijalisti",
                                    selected = activeAnalyticsTab == AnalyticsTab.Reps
                                ) { go(DashConfig.Analytics(AnalyticsTab.Reps)) }
                            }
                            AnalyticsSubItem(
                                text = "Analitika kupaca",
                                selected = activeAnalyticsTab == AnalyticsTab.Customers
                            ) { go(DashConfig.Analytics(AnalyticsTab.Customers)) }
                            AnalyticsSubItem(
                                text = "Kupci koji zahtijevaju pažnju",
                                selected = activeInstance is DashChild.AnalyticsAtRisk
                            ) { go(DashConfig.AnalyticsAtRisk) }
                            AnalyticsSubItem(
                                text = "Proizvodi i kategorije",
                                selected = activeInstance is DashChild.AnalyticsProducts
                            ) { go(DashConfig.AnalyticsProducts) }
                        }
                        DrawerItem(
                            text = "Narudžbe",
                            icon = vectorResource(Res.drawable.ic_k_cart),
                            selected = activeInstance is DashChild.Orders || activeInstance is DashChild.OrderDetails
                        ) { go(DashConfig.Orders) }
                        DrawerItem(
                            text = "Rabati",
                            icon = vectorResource(Res.drawable.ic_k_tag),
                            selected = activeInstance is DashChild.Customers || activeInstance is DashChild.CustomerRuleEditor
                        ) { go(DashConfig.Customers) }
                        if (canViewEmployees) {
                            DrawerItem(
                                text = "Komercijalisti",
                                icon = vectorResource(Res.drawable.ic_k_users),
                                selected = activeInstance is DashChild.Employees || activeInstance is DashChild.EmployeeLocations
                            ) { go(DashConfig.Employees) }
                        }

                        DrawerSection("Poruke")
                        DrawerItem(
                            text = "Poruke kupaca",
                            icon = vectorResource(Res.drawable.ic_k_chat),
                            selected = activeInstance is DashChild.CustomerMessages,
                            badge = messageState.value.vendorCustomer
                        ) { go(DashConfig.CustomerMessages) }
                        DrawerItem(
                            text = "Poruke admina",
                            icon = vectorResource(Res.drawable.ic_k_shield),
                            selected = activeInstance is DashChild.AdminMessages,
                            badge = messageState.value.vendorAdmin
                        ) { go(DashConfig.AdminMessages) }
                        DrawerItem(
                            text = "Interne poruke",
                            icon = vectorResource(Res.drawable.ic_k_inbox),
                            selected = activeInstance is DashChild.InternalMessages,
                            badge = messageState.value.staff
                        ) { go(DashConfig.InternalMessages) }

                        DrawerSection("Nalog")
                        DrawerItem(
                            text = "Korisnički profil",
                            icon = vectorResource(Res.drawable.ic_k_user),
                            selected = activeInstance is DashChild.Profile
                        ) { go(DashConfig.Profile) }
                    }

                    KDivider()
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(KarikaUiColors.RedSoft)
                                .clickable { component.logout() }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KIcon(
                                icon = vectorResource(Res.drawable.ic_k_logout_left),
                                tint = KarikaUiColors.Red,
                                size = 20.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            KarikaText(
                                text = "Odjavi se",
                                color = KarikaUiColors.Red,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W700
                            )
                        }
                        KarikaText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = KarikaUiColors.Subtle,
                            textSize = 12.sp,
                            text = appVersionName(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
        ) {
            KarikaScaffold(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = KarikaUiColors.Page,
                topBar = {
                    // Detail screens draw their own header with a back button
                    if (!activeInstance.hasOwnHeader()) {
                        TopBarDashboard(
                            component = component,
                            title = activeInstance.title(),
                            roundedBottom = !activeInstance.continuesHeader(),
                            menu = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                            action = {
                                component.dashNavigate(DashConfig.Notifications)
                            }
                        )
                    }
                },
                component = component
            ) {

                Children(stack = component.stack) {
                    when (val child = it.instance) {
                        is DashChild.Analytics -> AnalyticsView(child.component)
                        is DashChild.AnalyticsProducts -> AnalyticsProductsView(child.component)
                        is DashChild.AnalyticsAtRisk -> AnalyticsAtRiskView(child.component)
                        is DashChild.AnalyticsFilters -> AnalyticsFiltersView(child.component)

                        is DashChild.Orders -> OrdersView(child.component)
                        is DashChild.OrderDetails -> OrderDetailsView(child.component)

                        is DashChild.Products -> ProductsView(child.component)
                        is DashChild.ProductDetails -> ProductDetailsView(child.component)

                        is DashChild.Customers -> CustomersView(child.component)
                        is DashChild.CustomerRuleEditor -> CustomerRuleEditorView(child.component)

                        is DashChild.Employees -> EmployeesView(child.component)
                        is DashChild.EmployeeLocations -> EmployeeLocationsView(child.component)

                        is DashChild.CustomerMessages -> CustomerMessagesView(child.component)
                        is DashChild.AdminMessages -> AdminMessagesView(child.component)
                        is DashChild.InternalMessages -> InternalMessagesView(child.component)
                        is DashChild.MessageDetails -> MessagesOverviewView(child.component)

                        is DashChild.Profile -> ProfileView(child.component)

                        is DashChild.Notifications -> NotificationsView(child.component)
                    }
                }
            }
        }
    }
}

/** Title of the supplier header for the active screen. */
private fun DashChild.title(): String = when (this) {
    is DashChild.Analytics, is DashChild.AnalyticsProducts, is DashChild.AnalyticsAtRisk,
    is DashChild.AnalyticsFilters -> "Analitika"
    is DashChild.Orders, is DashChild.OrderDetails -> "Narudžbe"
    is DashChild.Customers, is DashChild.CustomerRuleEditor -> "Rabati"
    is DashChild.Employees, is DashChild.EmployeeLocations -> "Komercijalisti"
    is DashChild.Products, is DashChild.ProductDetails -> "Artikli"
    is DashChild.CustomerMessages -> "Poruke kupaca"
    is DashChild.AdminMessages -> "Poruke admina"
    is DashChild.InternalMessages -> "Interne poruke"
    is DashChild.MessageDetails -> "Poruke"
    is DashChild.Profile -> "Profil"
    is DashChild.Notifications -> "Notifikacije"
}

/** Screens with their own back header instead of the supplier header. */
private fun DashChild.hasOwnHeader(): Boolean =
    this is DashChild.OrderDetails || this is DashChild.CustomerRuleEditor || this is DashChild.MessageDetails

/** Screens that continue the white header with their own content (search, tabs). */
private fun DashChild.continuesHeader(): Boolean =
    this is DashChild.Orders || this is DashChild.Profile

/** Drawer row: icon and label, highlighted with the supplier accent when selected, optional badge. */
@Composable
private fun DrawerItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    badge: Int = 0,
    trailing: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) VendorAccentSoft else KarikaColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(
            icon = icon,
            tint = if (selected) VendorAccent else KarikaUiColors.Ink,
            size = 21.dp
        )
        Spacer(Modifier.width(14.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = if (selected) VendorAccent else KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            maxLines = 1
        )
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(VendorAccent)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "$badge",
                    color = KarikaColors.White,
                    textSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
        }
        trailing()
    }
}

/** Small uppercase heading of a drawer group ("PORUKE", "NALOG"). */
@Composable
private fun DrawerSection(title: String) {
    KarikaText(
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp),
        text = title.uppercase(),
        color = KarikaUiColors.Subtle,
        textSize = 11.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun AnalyticsSubItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(start = 46.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) VendorAccent else KarikaUiColors.Border)
        )
        Spacer(modifier = Modifier.width(12.dp))
        KarikaText(
            text = text,
            color = if (selected) VendorAccent else KarikaUiColors.Muted,
            textSize = 14.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            maxLines = 1
        )
    }
}
