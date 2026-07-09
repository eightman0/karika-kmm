package karika.distribucija.ba.ui.view.salesrep.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.ui.common.appVersionName
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLogo
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.salesrep.customers.SalesCustomersView
import karika.distribucija.ba.ui.view.salesrep.customers.SalesNewCustomerView
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesCustomerDetailView
import karika.distribucija.ba.ui.view.salesrep.customers.detail.SalesDiscountFormView
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminConversationView
import karika.distribucija.ba.ui.view.salesrep.messages.admin.SalesAdminMessagesView
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerConversationView
import karika.distribucija.ba.ui.view.salesrep.messages.customer.SalesCustomerMessagesView
import karika.distribucija.ba.ui.view.salesrep.messages.internal.SalesInternalMessagesView
import karika.distribucija.ba.ui.view.salesrep.operations.SalesOperationsView
import karika.distribucija.ba.ui.view.salesrep.orders.SalesOrdersView
import karika.distribucija.ba.ui.view.salesrep.orders.detail.SalesOrderDetailView
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_action
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_customers
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_logout
import karikav2.composeapp.generated.resources.ic_menu
import karikav2.composeapp.generated.resources.ic_messages
import karikav2.composeapp.generated.resources.ic_orders
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesDashboardView(component: SalesDashboardComponent) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val navState = component.stack.subscribeAsState()

    BoxWithConstraints(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(maxWidth * 0.82f),
                    drawerContainerColor = KarikaColors.Gray20,
                    drawerShape = RoundedCornerShape(0.dp)
                ) {
                    // ── Profile header ──────────────────────────────────────
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar circle with initials
                            KarikaLogo(size = 52)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                KarikaText(
                                    text = "Komercijalista",
                                    color = KarikaColors.Gray2,
                                    textSize = 16.sp,
                                    fontWeight = FontWeight.W700
                                )
                                KarikaText(
                                    text = "B2B Prodaja",
                                    color = KarikaColors.Gray6,
                                    textSize = 13.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                            // Close button
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .onClick { coroutineScope.launch { drawerState.close() } },
                                imageVector = vectorResource(Res.drawable.ic_tertiary),
                                contentDescription = "",
                                tint = KarikaColors.Gray6
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = KarikaColors.Gray9)
                    }

                    // ── Navigation items ────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_orders),
                            text = "Upravljanje narudžbama",
                            selected = navState.value.active.instance is SalesChild.Orders,
                            onClick = {
                                component.salesRepNavigate(SalesRepConfig.Orders, replace = true)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_customers),
                            text = "Upravljanje kupcima",
                            selected = navState.value.active.instance is SalesChild.Customers,
                            onClick = {
                                component.salesRepNavigate(SalesRepConfig.Customers, replace = true)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_messages),
                            text = "Poruke kupaca",
                            selected = navState.value.active.instance is SalesChild.CustomerMessages,
                            badge = 0, // TODO: wire to unread count
                            onClick = {
                                component.salesRepNavigate(
                                    SalesRepConfig.CustomerMessages,
                                    replace = true
                                )
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_email),
                            text = "Poruke admina",
                            selected = navState.value.active.instance is SalesChild.AdminMessages,
                            onClick = {
                                component.salesRepNavigate(
                                    SalesRepConfig.AdminMessages,
                                    replace = true
                                )
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_messages),
                            text = "Interne poruke",
                            selected = navState.value.active.instance is SalesChild.InternalMessages,
                            onClick = {
                                component.salesRepNavigate(
                                    SalesRepConfig.InternalMessages,
                                    replace = true
                                )
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                        SalesNavItem(
                            icon = vectorResource(Res.drawable.ic_action),
                            text = "Operacije",
                            selected = navState.value.active.instance is SalesChild.Operations,
                            onClick = {
                                component.salesRepNavigate(
                                    SalesRepConfig.Operations,
                                    replace = true
                                )
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }

                    // ── Footer ──────────────────────────────────────────────
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        HorizontalDivider(color = KarikaColors.Gray9)
                        Spacer(Modifier.height(8.dp))

                        // Logout row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { component.logout() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_logout),
                                contentDescription = "",
                                tint = KarikaColors.Error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            KarikaText(
                                text = "Odjavi se",
                                color = KarikaColors.Error,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W700
                            )
                        }

                        // Version + status dots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                text = appVersionName(),
                                color = KarikaColors.Gray7,
                                textSize = 12.sp,
                                fontWeight = FontWeight.W400
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(KarikaColors.Green1)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(KarikaColors.Green1)
                                )
                            }
                        }
                    }
                }
            }
        ) {
            KarikaScaffold(
                modifier = Modifier
                    .hideKeyboard()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = KarikaColors.White,
                topBar = {
                    val menuClick = { coroutineScope.launch { drawerState.open() } }
                    when (val child = navState.value.active.instance) {
                        is SalesChild.Orders -> SalesRootTopBar("Upravljanje narudžbama") { menuClick() }
                        is SalesChild.Customers -> SalesRootTopBar("Upravljanje kupcima") { menuClick() }
                        is SalesChild.CustomerMessages -> SalesRootTopBar("Poruke kupaca") { menuClick() }
                        is SalesChild.AdminMessages -> SalesRootTopBar("Poruke admina") { menuClick() }
                        is SalesChild.InternalMessages -> SalesRootTopBar("Interne poruke") { menuClick() }
                        is SalesChild.Operations -> SalesRootTopBar("Operacije") { menuClick() }
                        is SalesChild.CustomerDetail -> SalesDetailTopBar(
                            title = child.component.customer.fullName,
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.OrderDetail -> SalesDetailTopBar(
                            title = "Narudžba #${child.component.order.incrementId}",
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

                        is SalesChild.AdminConversation -> SalesDetailTopBar(
                            title = child.component.conversation.subject ?: "Poruka",
                            onBack = { child.component.goBack() }
                        )

                        is SalesChild.CustomerConversation -> SalesDetailTopBar(
                            title = child.component.conversation.customerName(),
                            onBack = { child.component.goBack() }
                        )
                    }
                },
                component = component
            ) {
                Children(stack = component.stack) {
                    when (val child = it.instance) {
                        is SalesChild.Orders -> SalesOrdersView(child.component)
                        is SalesChild.Customers -> SalesCustomersView(child.component)
                        is SalesChild.CustomerMessages -> SalesCustomerMessagesView(child.component)
                        is SalesChild.AdminMessages -> SalesAdminMessagesView(child.component)
                        is SalesChild.InternalMessages -> SalesInternalMessagesView(child.component)
                        is SalesChild.Operations -> SalesOperationsView(child.component)
                        is SalesChild.CustomerDetail -> SalesCustomerDetailView(child.component)
                        is SalesChild.OrderDetail -> SalesOrderDetailView(child.component)
                        is SalesChild.DiscountForm -> SalesDiscountFormView(child.component)
                        is SalesChild.NewCustomer -> SalesNewCustomerView(child.component)
                        is SalesChild.AdminConversation -> SalesAdminConversationView(child.component)
                        is SalesChild.CustomerConversation -> SalesCustomerConversationView(child.component)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KarikaColors.White
        ),
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KarikaLogo(size = 40)
            }
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick { onMenuClick() }
                    .padding(horizontal = 4.dp),
                imageVector = vectorResource(Res.drawable.ic_menu),
                contentDescription = "",
                tint = KarikaColors.Gray2
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesRootTopBar(title: String, onMenuClick: () -> Unit = {}) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = KarikaColors.White),
        title = {
            KarikaText(
                text = title,
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick { onMenuClick() }
                    .padding(horizontal = 4.dp),
                imageVector = vectorResource(Res.drawable.ic_menu),
                contentDescription = "",
                tint = KarikaColors.Gray2
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesDetailTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = KarikaColors.White),
        title = {
            KarikaText(
                text = title,
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick { onBack() }
                    .padding(horizontal = 4.dp),
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                contentDescription = "Nazad",
                tint = KarikaColors.Blue
            )
        }
    )
}

@Composable
private fun SalesNavItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit
) {
    val bgColor = if (selected) KarikaColors.Blue else KarikaColors.Transparent
    val contentColor = if (selected) KarikaColors.White else KarikaColors.Gray6

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color = bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = contentColor,
            textSize = 15.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            textAlign = TextAlign.Start
        )
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(KarikaColors.Blue),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "$badge",
                    color = KarikaColors.White,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}
