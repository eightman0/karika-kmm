package karika.distribucija.ba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import karika.distribucija.ba.ui.common.CommonComponent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ImagePreview(component: CommonComponent) {
    val image by component.stateHolder.imagePreview.asState()

    if (image != null) {
        Box(
            modifier = Modifier
                .hideKeyboard()
                .background(color = KarikaColors.White)
                .fillMaxSize()
        ) {
            ZoomableImage(
                painter = rememberAsyncImagePainter(image),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
                minScale = 1f,
                maxScale = 5f,
                doubleTapScale = 2f
            )
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .onClick {
                            component.showImagePreview(null)
                        }
                        .background(color = KarikaColors.Primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp),
                        imageVector = vectorResource(Res.drawable.ic_tertiary),
                        tint = KarikaColors.White,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    painter: Painter,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    doubleTapScale: Float = 2f
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        val scaleChange = newScale / scale

                        val newOffset = (offset - centroid) * scaleChange + centroid + pan

                        scale = newScale
                        offset = clampToBounds(newOffset, scale, containerSize)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { focal ->
                        val target = if (scale < doubleTapScale) doubleTapScale else 1f
                        val k = (target.coerceIn(minScale, maxScale)) / scale
                        val newOffset = (offset - focal) * k + focal
                        scale = (scale * k).coerceIn(minScale, maxScale)
                        offset = clampToBounds(newOffset, scale, containerSize)
                    }
                )
            }
            .graphicsLayer(
                transformOrigin = TransformOrigin(0f, 0f),
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    )
}

private fun clampToBounds(o: Offset, scale: Float, box: IntSize): Offset {
    if (box.width == 0 || box.height == 0) return Offset.Zero
    val maxX = box.width * (scale - 1f)
    val maxY = box.height * (scale - 1f)
    val x = o.x.coerceIn(-maxX, 0f)
    val y = o.y.coerceIn(-maxY, 0f)
    return Offset(x, y)
}

