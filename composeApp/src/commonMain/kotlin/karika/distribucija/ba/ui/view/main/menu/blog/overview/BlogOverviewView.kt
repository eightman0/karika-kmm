package karika.distribucija.ba.ui.view.main.menu.blog.overview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack

@Composable
fun BlogOverviewView(component: BlogOverviewComponent) {
    val blog by component.blog.collectAsState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack(blog.title ?: "") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            key(blog.content ?: "test") {
                BlogItem(blog, component)
            }
        }
    }
}

@Composable
private fun BlogItem(blog: Blog, component: BlogOverviewComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray18,
            text = blog.date?.split(" ")?.first(),
            textSize = 14.sp,
            fontWeight = FontWeight.W300
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            text = blog.title,
            textSize = 16.sp,
            fontWeight = FontWeight.W500
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .border(width = 1.dp, color = KarikaColors.Gray5)
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxWidth(),
                model = blog.image()
            )
        }
        HtmlTextWithStyles(
            modifier = Modifier
                .fillMaxWidth(),
            html = blog.content ?: "",
            textColor = KarikaColors.Gray2
        )
    }
}