package karika.distribucija.ba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_primary_logo
import karikav2.composeapp.generated.resources.img_background
import karikav2.composeapp.generated.resources.img_landing_banner
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun KarikaBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(KarikaColors.Background1, KarikaColors.Background2),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(Res.drawable.img_background),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
        content.invoke()
    }
}

@Composable
fun KarikaLogo(size: Int = 88, onClick: () -> Unit = {}) {
    Image(
        modifier = Modifier
            .onClick {
                onClick.invoke()
            }
            .size(size.dp),
        imageVector = vectorResource(Res.drawable.ic_primary_logo),
        contentDescription = ""
    )
}

@Composable
fun LandingBanner() {
    Image(
        modifier = Modifier
            .fillMaxWidth(),
        painter = painterResource(Res.drawable.img_landing_banner),
        contentDescription = ""
    )
}
