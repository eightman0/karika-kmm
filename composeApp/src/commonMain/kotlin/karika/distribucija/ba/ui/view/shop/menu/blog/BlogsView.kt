package karika.distribucija.ba.ui.view.shop.menu.blog

import karika.distribucija.ba.util.inSarajevo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.isTabletLandscape
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BlogsView(component: BlogsComponent) {
    val blogs by component.blogs.collectAsState()
    val columns = if (isTabletLandscape()) 2 else 1
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(title = "Svi blog članci", onBack = { component.appBack() })
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = blogs.chunked(columns)) { items ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            BlogItem(blog = item, component = component)
                        }
                    }
                    if (items.size < columns) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlogItem(blog: Blog, component: BlogsComponent) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = { component.navigateToBlog(blog) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KImage(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(10.dp)),
                url = blog.image()
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KarikaText(
                    color = KarikaUiColors.Muted,
                    text = blog.date?.inSarajevo()?.split(" ")?.first(),
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1
                )
                KarikaText(
                    color = KarikaUiColors.Ink,
                    text = blog.title,
                    textSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 2
                )
                KarikaText(
                    color = KarikaUiColors.Muted,
                    text = blog.desc,
                    textSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(8.dp))
            KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaUiColors.Subtle, size = 18.dp)
        }
    }
}
