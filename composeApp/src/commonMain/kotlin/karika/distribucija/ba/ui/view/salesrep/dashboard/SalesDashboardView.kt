package karika.distribucija.ba.ui.view.salesrep.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KarikaLogo
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.ReadFilterDropdown
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsFiltersView
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsTab
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsView
import karika.distribucija.ba.ui.view.distributer.analytics.atrisk.AnalyticsAtRiskView
import karika.distribucija.ba.ui.view.distributer.analytics.products.AnalyticsProductsView
import karika.distribucija.ba.ui.view.salesrep.cart.SalesOrderCartView
import karika.distribucija.ba.ui.view.salesrep.cart.SalesOrderReviewView
import karika.distribucija.ba.ui.view.salesrep.catalog.SalesOrderCatalogView
import karika.distribucija.ba.ui.view.salesrep.customers.SalesCustomersView
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesCustomerDetailView
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesDiscountFormView
import karika.distribucija.ba.ui.view.salesrep.customers.invite.SalesInviteCustomerView
import karika.distribucija.ba.ui.view.salesrep.customers.newcustomer.SalesNewCustomerView
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminConversationView
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminMessagesView
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminNewMessageView
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerConversationView
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerMessagesView
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerNewMessageView
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalConversationView
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalMessagesView
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalNewMessageView
import karika.distribucija.ba.ui.view.salesrep.notifications.SalesNotificationsView
import karika.distribucija.ba.ui.view.salesrep.operations.SalesOperationsView
import karika.distribucija.ba.ui.view.salesrep.orders.SalesOrdersView
import karika.distribucija.ba.ui.view.salesrep.orders.detail.SalesOrderDetailView
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_chart
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_chevron_left
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_logout_left
import karikav2.composeapp.generated.resources.ic_k_menu
import karikav2.composeapp.generated.resources.ic_k_users
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

/** Test tag of the sales rep's menu icon, for the end-to-end tests. */
const val SALES_MENU_TAG = "sales_menu"

@Composable
fun SalesDashboardView(component: SalesDashboardComponent) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val navState = component.stack.subscribeAsState()
    val salesManager by component.stateHolder.salesSpecificHandler.me.collectAsState()
    val notificationBadge by component.stateHolder.vendorNotificationHandler.notificationCount.collectAsState()
    val messageState by component.stateHolder.vendorNotificationHandler.chatUnreadCount.asState()
    val activeInstance = navState.value.active.instance
    val isAnalyticsActive = activeInstance is SalesChild.Analytics ||
        activeInstance is SalesChild.AnalyticsProducts ||
        activeInstance is SalesChild.AnalyticsAtRisk
    var analyticsExpanded by remember { mutableStateOf(false) }
    val activeAnalyticsTab = if (activeInstance is SalesChild.Analytics) {
        activeInstance.component.selectedTab.collectAsState().value
    } else {
        null
    }
    val isMessagesActive = activeInstance is SalesChild.CustomerMessages ||
        activeInstance is SalesChild.AdminMessages ||
        activeInstance is SalesChild.InternalMessages
    var messagesExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.Gray20)
                .align(Alignment.BottomCenter)
                .height(100.dp)
        )
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(minOf(maxWidth * 0.82f, 340.dp)),
                    drawerContainerColor = KarikaColors.White,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    // ── Profile header ──────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KInitials(
                            name = salesManager.name,
                            size = 48.dp,
                            shape = CircleShape,
                            background = VendorAccent,
                            color = KarikaColors.White,
                            textSize = 16.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            KarikaText(
                                text = "${salesManager.name}",
                                color = KarikaUiColors.Ink,
                                textSize = 16.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.W700,
                                maxLines = 1
                            )
                            KarikaText(
                                text = "Komercijalista",
                                color = KarikaUiColors.Muted,
                                textSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        KCircleButton(
                            icon = vectorResource(Res.drawable.ic_k_close),
                            size = 40.dp,
                            iconSize = 18.dp
                        ) {
                            coroutineScope.launch { drawerState.close() }
                        }
                    }
                    KDivider()

                    // ── Navigation items ────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SalesExpandableNavItem(
                            icon = vectorResource(Res.drawable.ic_k_chart),
                            text = "Analitika",
                            expanded = analyticsExpanded,
                            selected = isAnalyticsActive,
                            onClick = { analyticsExpanded = !analyticsExpanded }
                        )
                        if (analyticsExpanded) {
                            SalesSubNavItem(
                                text = "Pregled",
                                selected = activeAnalyticsTab == AnalyticsTab.Overview,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.Analytics(AnalyticsTab.Overview), true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            SalesSubNavItem(
                                text = "Trendovi prodaje",
                                selected = activeAnalyticsTab == AnalyticsTab.Trends,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.Analytics(AnalyticsTab.Trends), true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            if (salesManager.capabilities.canSeeDashboard) {
                                SalesSubNavItem(
                                    text = "Komercijalisti",
                                    selected = activeAnalyticsTab == AnalyticsTab.Reps,
                                    onClick = {
                                        component.salesRepNavigate(SalesRepConfig.Analytics(AnalyticsTab.Reps), true)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                            SalesSubNavItem(
                                text = "Analitika kupaca",
                                selected = activeAnalyticsTab == AnalyticsTab.Customers,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.Analytics(AnalyticsTab.Customers), true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            SalesSubNavItem(
                                text = "Kupci koji zahtijevaju pažnju",
                                selected = navState.value.active.instance is SalesChild.AnalyticsAtRisk,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.AnalyticsAtRisk, true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            SalesSubNavItem(
                                text = "Proizvodi i kategorije",
                                selected = navState.value.active.instance is SalesChild.AnalyticsProducts,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.AnalyticsProducts, true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_k_cart),
                            text = "Upravljanje narudžbama",
                            selected = navState.value.active.instance is SalesChild.Orders,
                            onClick = {
                                component.salesRepNavigate(SalesRepConfig.Orders, replace = true)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_k_users),
                            text = "Upravljanje kupcima",
                            selected = navState.value.active.instance is SalesChild.Customers,
                            onClick = {
                                component.salesRepNavigate(SalesRepConfig.Customers, replace = true)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesExpandableNavItem(
                            icon = vectorResource(Res.drawable.ic_k_chat),
                            text = "Poruke",
                            expanded = messagesExpanded,
                            selected = isMessagesActive,
                            badge = messageState.staff + messageState.vendorAdmin + messageState.vendorCustomer,
                            onClick = { messagesExpanded = !messagesExpanded }
                        )
                        if (messagesExpanded) {
                            SalesSubNavItem(
                                text = "Interne poruke",
                                selected = navState.value.active.instance is SalesChild.InternalMessages,
                                badge = messageState.staff,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.InternalMessages, replace = true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            SalesSubNavItem(
                                text = "Poruke admina",
                                selected = navState.value.active.instance is SalesChild.AdminMessages,
                                badge = messageState.vendorAdmin,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.AdminMessages, replace = true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            SalesSubNavItem(
                                text = "Poruke kupaca",
                                selected = navState.value.active.instance is SalesChild.CustomerMessages,
                                badge = messageState.vendorCustomer,
                                onClick = {
                                    component.salesRepNavigate(SalesRepConfig.CustomerMessages, replace = true)
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                        //SalesNavItem(
                        //    icon = vectorResource(Res.drawable.ic_action),
                        //    text = "Operacije",
                        //    selected = navState.value.active.instance is SalesChild.Operations,
                        //    onClick = {
                        //        component.salesRepNavigate(
                        //            SalesRepConfig.Operations,
                        //            replace = true
                        //        )
                        //        coroutineScope.launch { drawerState.close() }
                        //    }
                        //)
                    }

                    // ── Footer ──────────────────────────────────────────────
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                text = appVersionName(),
                                color = KarikaUiColors.Subtle,
                                textSize = 12.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(KarikaUiColors.GreenDot)
                            )
                        }
                    }
                }
            }
        ) {
            KarikaScaffold(
                modifier = Modifier
                    .hideKeyboard()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = KarikaUiColors.Page,
                topBar = {
                    val menuClick = { coroutineScope.launch { drawerState.open() } }
                    val onNotifications = { component.salesRepNavigate(SalesRepConfig.Notifications) }
                    when (val child = navState.value.active.instance) {
                        is SalesChild.Analytics -> SalesRootTopBar("Analitika", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.AnalyticsProducts -> SalesRootTopBar("Analitika", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.AnalyticsAtRisk -> SalesRootTopBar("Analitika", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.AnalyticsFilters -> {}
                        is SalesChild.Orders -> SalesRootTopBar("Upravljanje narudžbama", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.Customers -> SalesRootTopBar("Upravljanje kupcima", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.CustomerMessages -> SalesRootTopBar("Poruke kupaca", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.AdminMessages -> SalesRootTopBar("Poruke admina", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.InternalMessages -> SalesRootTopBar("Interne poruke", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.Operations -> SalesRootTopBar("Operacije", notificationBadge, onNotifications) { menuClick() }
                        is SalesChild.Notifications -> SalesDetailTopBar(
                            title = "Obavijesti",
                            onBack = { child.component.goBack() },
                            actions = {
                                val readFilter by child.component.readFilter.collectAsState()
                                ReadFilterDropdown(
                                    selected = readFilter,
                                    onSelect = { child.component.setReadFilter(it) }
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                        )
                        is SalesChild.CustomerDetail -> SalesDetailTopBar(
                            title = child.component.customer.company?.takeIf { it.isNotBlank() }
                                ?: child.component.customer.fullName,
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.OrderDetail -> SalesDetailTopBar(
                            title = "Narudžba #${child.component.vendorOrder.value.orderId}",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.DiscountForm -> SalesDetailTopBar(
                            title = if (child.component.isEdit) "Izmijeni popust" else "Novi popust",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.NewCustomer -> SalesDetailTopBar(
                            title = "Novi kupac",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.InviteCustomer -> SalesDetailTopBar(
                            title = "Pozovi kupca",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.AdminConversation -> SalesDetailTopBar(
                            title = child.component.conversation.counterpartName ?: "Poruka",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.AdminNewMessage -> SalesDetailTopBar(
                            title = "Nova poruka",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.CustomerNewMessage -> {
                            val selectedCustomer by child.component.selectedCustomer.collectAsState()
                            SalesDetailTopBar(
                                title = selectedCustomer?.name ?: "Nova poruka",
                                onBack = { child.component.goBack() }
                            )
                        }

                        is SalesChild.CustomerConversation -> SalesDetailTopBar(
                            title = child.component.conversation.counterpartName ?: "-",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.InternalConversation -> SalesDetailTopBar(
                            title = child.component.conversation.counterpartName ?: "-",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.InternalNewMessage -> SalesDetailTopBar(
                            title = "Nova interna poruka",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.OrderCatalog -> SalesDetailTopBar(
                            title = "Naruči: ${child.component.customer.company}",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.OrderCart -> SalesDetailTopBar(
                            title = "Korpa: ${child.component.customer.company}",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.OrderReview -> SalesDetailTopBar(
                            title = "Pregled narudžbe",
                            onBack = { child.component.goBack() }
                        )
                    }
                },
                component = component
            ) {
                Children(stack = component.stack) {
                    when (val child = it.instance) {
                        is SalesChild.Analytics -> AnalyticsView(child.component)
                        is SalesChild.AnalyticsProducts -> AnalyticsProductsView(child.component)
                        is SalesChild.AnalyticsAtRisk -> AnalyticsAtRiskView(child.component)
                        is SalesChild.AnalyticsFilters -> AnalyticsFiltersView(child.component)
                        is SalesChild.Orders -> SalesOrdersView(child.component)
                        is SalesChild.Customers -> SalesCustomersView(child.component)
                        is SalesChild.CustomerMessages -> SalesCustomerMessagesView(child.component)
                        is SalesChild.AdminMessages -> SalesAdminMessagesView(child.component)
                        is SalesChild.InternalMessages -> SalesInternalMessagesView(child.component)
                        is SalesChild.InternalConversation -> SalesInternalConversationView(child.component)
                        is SalesChild.InternalNewMessage -> SalesInternalNewMessageView(child.component)
                        is SalesChild.Operations -> SalesOperationsView(child.component)
                        is SalesChild.CustomerDetail -> SalesCustomerDetailView(child.component)
                        is SalesChild.OrderDetail -> SalesOrderDetailView(child.component)
                        is SalesChild.DiscountForm -> SalesDiscountFormView(child.component)
                        is SalesChild.NewCustomer -> SalesNewCustomerView(child.component)
                        is SalesChild.AdminConversation -> SalesAdminConversationView(child.component)
                        is SalesChild.CustomerConversation -> SalesCustomerConversationView(child.component)
                        is SalesChild.AdminNewMessage -> SalesAdminNewMessageView(child.component)
                        is SalesChild.CustomerNewMessage -> SalesCustomerNewMessageView(child.component)
                        is SalesChild.OrderCatalog -> SalesOrderCatalogView(child.component)
                        is SalesChild.OrderCart -> SalesOrderCartView(child.component)
                        is SalesChild.OrderReview -> SalesOrderReviewView(child.component)
                        is SalesChild.InviteCustomer -> SalesInviteCustomerView(child.component)
                        is SalesChild.Notifications -> SalesNotificationsView(child.component)
                    }
                }
            }
        }
    }
}

/** White header of the sales rep's main screens: menu, title and the notifications bell. */
@Composable
private fun SalesRootTopBar(
    title: String,
    notificationBadge: Int = 0,
    onNotifications: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KCircleButton(
            modifier = Modifier.testTag(SALES_MENU_TAG),
            icon = vectorResource(Res.drawable.ic_k_menu),
            onClick = onMenuClick
        )
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        Box {
            KCircleButton(
                modifier = Modifier.semantics { contentDescription = "Obavijesti" },
                icon = vectorResource(Res.drawable.ic_k_bell),
                onClick = onNotifications
            )
            if (notificationBadge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(VendorAccent)
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = if (notificationBadge > 9) "9+" else "$notificationBadge",
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700,
                        color = KarikaColors.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** White header of the sales rep's detail screens: back, title and optional actions. */
@Composable
fun SalesDetailTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KCircleButton(
            modifier = Modifier.semantics { contentDescription = "Nazad" },
            icon = vectorResource(Res.drawable.ic_k_chevron_left),
            onClick = onBack
        )
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W700,
            maxLines = 2
        )
        actions()
    }
}

@Composable
private fun SalesExpandableNavItem(
    icon: ImageVector,
    text: String,
    expanded: Boolean,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit
) {
    SalesNavItem(icon = icon, text = text, selected = selected, badge = badge, onClick = onClick, trailing = {
        KIcon(
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
            icon = vectorResource(Res.drawable.ic_k_chevron_down),
            tint = if (selected) VendorAccent else KarikaUiColors.Muted,
            size = 18.dp
        )
    })
}

@Composable
private fun SalesSubNavItem(text: String, selected: Boolean, badge: Int = 0, onClick: () -> Unit) {
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
            modifier = Modifier.weight(1f),
            text = text,
            color = if (selected) VendorAccent else KarikaUiColors.Muted,
            textSize = 14.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            maxLines = 1
        )
        if (badge > 0) {
            NavBadge(badge)
        }
    }
}

@Composable
private fun SalesNavItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {},
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
            textAlign = TextAlign.Start,
            maxLines = 1
        )
        if (badge > 0) {
            NavBadge(badge)
        }
        trailing()
    }
}

@Composable
private fun NavBadge(count: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .clip(RoundedCornerShape(50))
            .background(VendorAccent)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = "$count",
            color = KarikaColors.White,
            textSize = 11.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}
