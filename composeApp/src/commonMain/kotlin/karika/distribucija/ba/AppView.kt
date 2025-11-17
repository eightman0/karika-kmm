package karika.distribucija.ba

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.setSingletonImageLoaderFactory
import com.arkivanov.decompose.extensions.compose.stack.Children
import karika.distribucija.ba.ui.common.getEnvPrefix
import karika.distribucija.ba.ui.components.GuestUserInfoDialog
import karika.distribucija.ba.ui.components.ImagePreview
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.MandatoryUpdateModal
import karika.distribucija.ba.ui.components.ScreenSaver
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.distributer.dashboard.DashboardView
import karika.distribucija.ba.ui.view.main.MainView
import karika.distribucija.ba.ui.view.main.menu.blog.BlogsView
import karika.distribucija.ba.ui.view.main.menu.blog.overview.BlogOverviewView
import karika.distribucija.ba.ui.view.main.menu.faq.FaqView
import karika.distribucija.ba.ui.view.main.product.ProductView
import karika.distribucija.ba.ui.view.main.profile.account.AccountView
import karika.distribucija.ba.ui.view.main.profile.messages.admin.AdminMessagesView
import karika.distribucija.ba.ui.view.main.profile.messages.overview.MessagesOverviewView
import karika.distribucija.ba.ui.view.main.profile.messages.vendor.VendorMessagesView
import karika.distribucija.ba.ui.view.main.profile.notifications.NotificationsView
import karika.distribucija.ba.ui.view.main.profile.order.OrdersView
import karika.distribucija.ba.ui.view.main.profile.order.comments.CommentsView
import karika.distribucija.ba.ui.view.main.profile.order.details.OrderDetailsView
import karika.distribucija.ba.ui.view.main.profile.points.PointsView
import karika.distribucija.ba.ui.view.main.vendor.details.VendorDetailsView
import karika.distribucija.ba.ui.view.prelogin.PreLoginView
import karika.distribucija.ba.util.asyncImageLoader
import karika.distribucija.ba.util.enableDiskCache
import org.koin.compose.KoinContext

@Composable
fun App(component: AppComponent) {
    KoinContext {
        setSingletonImageLoaderFactory { context ->
            if (true) context.asyncImageLoader() else
                context.asyncImageLoader().enableDiskCache()
        }

        KarikaScaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = KarikaColors.Primary,
            component = component,
            disableSnackBar = false
        ) {
            Children(
                stack = component.stack
            ) {
                when (val child = it.instance) {
                    is Child.PreLogin -> PreLoginView(child.component)

                    is Child.Main -> MainView(child.component)
                    is Child.ProductDetails -> ProductView(child.component)
                    is Child.VendorDetails -> VendorDetailsView(child.component)
                    is Child.Account -> AccountView(child.component)
                    is Child.Blogs -> BlogsView(child.component)
                    is Child.Blog -> BlogOverviewView(child.component)

                    is Child.Orders -> OrdersView(child.component)
                    is Child.OrderDetails -> OrderDetailsView(child.component)
                    is Child.OrderComments -> CommentsView(child.component)

                    is Child.AdminMessages -> AdminMessagesView(child.component)
                    is Child.VendorMessages -> VendorMessagesView(child.component)
                    is Child.MessagesOverview -> MessagesOverviewView(child.component)

                    is Child.Points -> PointsView(child.component)
                    is Child.Notifications -> NotificationsView(child.component)

                    is Child.Faq -> FaqView(child.component)

                    //vendor_side
                    is Child.Dashboard -> DashboardView(child.component)
                }
            }
            ImagePreview(component)
            LoadingView1(component)
            ScreenSaver(component)
            getEnvPrefix()
                .replace(".", "")
                .takeIf { it.isNotEmpty() }
                ?.let {
                    WaterMarkBox(it)
                }
        }

        GuestUserInfoDialog(component)
        MandatoryUpdate(component)
    }
}

@Composable
private fun MandatoryUpdate(component: AppComponent) {
    val update by component.showMandatoryUpdate.asState()

    if (update) {
        MandatoryUpdateModal()
    }
}

@Composable
fun WaterMarkBox(watermarkText: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 40.dp)
                    .graphicsLayer {
                        rotationZ = 45f
                    }
                    .offset(x = 80.dp, y = (-10).dp)
                    .background(
                        color = when (getEnvPrefix()) {
                            "demo." -> KarikaColors.Yellow
                            "test." -> KarikaColors.Green3
                            else -> KarikaColors.Black
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = watermarkText,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    color = when (getEnvPrefix()) {
                        "demo." -> KarikaColors.Gray2
                        else -> KarikaColors.White
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
