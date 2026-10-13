package karika.distribucija.ba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_gift
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun KarikaImage(
    model: Any?,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier,
    placeholder: DrawableResource = Res.drawable.ic_gift
) {
    val imageVector = rememberVectorPainter(vectorResource(placeholder))

    if (model == null || (model as? String)?.isEmpty() == true) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = imageVector,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }
        return
    }

    // AsyncImage, not SubcomposeAsyncImage: a subcomposition per image in a lazy list could
    // fail with "Unsupported concurrent change during composition" while scrolling. While it
    // loads, the caller's background shows through.
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .crossfade(true)
            .precision(Precision.EXACT)
            .memoryCacheKey(model as? String?)
            .diskCacheKey(model as? String?)
            .data(model)
            .build(),
        error = imageVector,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )
}
