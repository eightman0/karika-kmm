package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.ui.view.distributer.dashboard.DashboardComponent
import karika.distribucija.ba.ui.view.main.MainChild
import karika.distribucija.ba.ui.view.main.MainComponent
import karika.distribucija.ba.ui.view.main.MainConfig
import karika.distribucija.ba.ui.view.main.search.SearchComponent
import karika.distribucija.ba.util.KarikaConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_action
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_menu
import karikav2.composeapp.generated.resources.ic_navigation_vendors
import karikav2.composeapp.generated.resources.ic_notifications
import karikav2.composeapp.generated.resources.ic_outlet
import karikav2.composeapp.generated.resources.ic_products
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBack(
    title: String,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    color: Color = KarikaColors.Primary,
    back: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = title,
                color = KarikaColors.White,
                textSize = 20.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick {
                        back()
                    }
                    .padding(horizontal = 4.dp),
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                contentDescription = "",
                tint = KarikaColors.White
            )
        },
        actions = {

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = color
        ),
        windowInsets = windowInsets
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar1(component: MainComponent) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Profil",
                color = KarikaColors.White,
                textSize = 20.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
        },
        actions = {

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KarikaColors.Primary
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(component: MainComponent) {
    val state by component.stack.subscribeAsState()

    if (state.active.instance is MainChild.VendorDetails) {
        return
    }
    if (state.active.instance is MainChild.ProductDetails) {
        return
    }
    if (state.active.instance is MainChild.Search) {
        return
    }
    if (state.active.instance is MainChild.Categories) {
        return
    }
    if (state.active.instance is MainChild.CategoryProducts) {
        return
    }

    if (state.active.instance is MainChild.Profile) {
        TopBar1(component)
    } else {
        Column {
            YSpacer8()
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    SearchBox(
                        enabled = false,
                        modifier = Modifier
                            .onClick {
                                component.mainNavigate(MainConfig.Search)
                            }
                            .padding(end = 16.dp)
                            .fillMaxWidth(),
                        onValueChange = {

                        },
                        onSearchExecute = {

                        }
                    )
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KarikaColors.Primary
                ),
            )
            YSpacer8()
            if (state.active.instance is MainChild.Home) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = KarikaColors.White,
                    thickness = 1.dp
                )
                ActionBar(component)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSearch(component: SearchComponent) {
    val focusRequester = FocusRequester()
    val value = component.searchText.asState()
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            SearchBox(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                onValueChange = {
                    value.value = it
                },
                onSearchExecute = {
                    if (value.value.length > 2) {
                        component.search(true)
                    }
                },
                onClose = {
                    component.search(true)
                },
                focusRequester = focusRequester,
                searchText = component.searchText
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick {
                        component.mainBack()
                    },
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                contentDescription = "",
                tint = KarikaColors.White
            )
        },
        actions = {

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KarikaColors.Primary
        ),
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun ActionBar(component: MainComponent) {
    if (component.isGuest()) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .background(color = KarikaColors.Primary)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTextItem(
                modifier = Modifier
                    .onClick {
                        component.navigate(MainConfig.Vendor)
                    }
                    .weight(1f)
                    .padding(start = 16.dp),
                icon = vectorResource(Res.drawable.ic_navigation_vendors),
                iconColor = KarikaColors.White,
                textColor = KarikaColors.White,
                textSize = 16.sp,
                fontWeight = FontWeight.W600,
                text = "DOBAVLJAČI"
            )
            IconTextItem(
                modifier = Modifier
                    .onClick {
                        component.mainNavigate(
                            MainConfig.CategoryProducts(
                                Category(
                                    id = 10,
                                    name = "SVI PROIZVODI"
                                )
                            )
                        )
                    }
                    .weight(1f),
                icon = vectorResource(Res.drawable.ic_products),
                iconColor = KarikaColors.White,
                textColor = KarikaColors.White,
                textSize = 16.sp,
                fontWeight = FontWeight.W600,
                text = "PROIZVODI"
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = KarikaColors.White,
            thickness = 1.dp
        )
    }
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(color = KarikaColors.Primary)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTextItem(
            modifier = Modifier
                .onClick {
                    component.mainNavigate(
                        MainConfig.CategoryProducts(
                            Category(
                                id = KarikaConfig.getOutletId(),
                                name = "OUTLET"
                            )
                        )
                    )
                }
                .weight(1f)
                .padding(start = 16.dp),
            icon = vectorResource(Res.drawable.ic_outlet),
            iconColor = KarikaColors.White,
            textColor = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W600,
            text = "OUTLET"
        )
        IconTextItem(
            modifier = Modifier
                .onClick {
                    component.mainNavigate(
                        MainConfig.CategoryProducts(
                            Category(
                                id = KarikaConfig.getActionId(),
                                name = "AKCIJE"
                            )
                        )
                    )
                }
                .weight(1f),
            icon = vectorResource(Res.drawable.ic_action),
            iconColor = KarikaColors.White,
            textColor = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W600,
            text = "AKCIJE"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarDashboard(
    component: DashboardComponent,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    menu: () -> Unit,
    action: () -> Unit
) {
    val badge by component.stateHolder.vendorNotificationHandler.notificationCount.collectAsState()

    Column {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth(),
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaLogo(size = 40)
                }
            },
            navigationIcon = {
                Icon(
                    modifier = Modifier
                        .onClick {
                            menu()
                        }
                        .padding(horizontal = 4.dp),
                    imageVector = vectorResource(Res.drawable.ic_menu),
                    contentDescription = "",
                    tint = KarikaColors.Gray2
                )
            },
            actions = {
                Box(modifier = Modifier) {
                    Icon(
                        modifier = Modifier
                            .onClick {
                                action()
                            }
                            .padding(horizontal = 4.dp),
                        imageVector = vectorResource(Res.drawable.ic_notifications),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                    if (badge > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .offset(16.dp, (-8).dp)
                                .background(color = KarikaColors.Red, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                modifier = Modifier
                                    .padding(0.dp),
                                text = "$badge",
                                textSize = 10.sp,
                                fontWeight = FontWeight.W400,
                                color = KarikaColors.White
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = KarikaColors.White
            ),
            windowInsets = windowInsets
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
    }
}
