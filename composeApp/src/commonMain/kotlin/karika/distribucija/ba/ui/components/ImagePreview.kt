package karika.distribucija.ba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import karika.distribucija.ba.ui.common.CommonComponent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlin.math.abs
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ImagePreview(component: CommonComponent) {
    val preview by component.stateHolder.imagePreview.asState()

    if (preview != null) {
        val images = preview!!.images
        val pagerState = rememberPagerState(initialPage = preview!!.startIndex) { images.size }

        Box(
            modifier = Modifier
                .hideKeyboard()
                .background(color = KarikaColors.White)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxSize()
            ) { page ->
                ZoomableImage(
                    painter = rememberAsyncImagePainter(images[page]),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    minScale = 1f,
                    maxScale = 5f,
                    doubleTapScale = 2f
                )
            }
            if (images.size > 1) {
                PagerIndicator(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    pageCount = images.size,
                    currentPage = pagerState.currentPage
                )
            }
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
                awaitEachGesture {
                    var pastTouchSlop = false
                    var accumulatedZoom = 1f
                    var accumulatedPan = Offset.Zero
                    val touchSlop = viewConfiguration.touchSlop

                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val multitouch = event.changes.size > 1

                            if (!pastTouchSlop) {
                                accumulatedZoom *= zoomChange
                                accumulatedPan += panChange
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - accumulatedZoom) * centroidSize
                                val panMotion = accumulatedPan.getDistance()
                                if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop && (multitouch || scale > 1f)) {
                                if (zoomChange != 1f || panChange != Offset.Zero) {
                                    val centroid = event.calculateCentroid(useCurrent = false)
                                    val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
                                    val scaleChange = newScale / scale
                                    val newOffset = (offset - centroid) * scaleChange + centroid + panChange

                                    scale = newScale
                                    offset = clampToBounds(newOffset, scale, containerSize)
                                }
                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        }
                    } while (!canceled && event.changes.any { it.pressed })
                }
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

