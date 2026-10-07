package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karika.distribucija.ba.ui.components.KarikaColors

@Composable
actual fun EmployeeRouteMap(
    points: List<EmployeeLocationPoint>,
    focus: EmployeeLocationPoint?,
    showRoute: Boolean,
    modifier: Modifier
) {
    val latLngs = remember(points) { points.map { LatLng(it.latitude, it.longitude) } }
    val cameraState = rememberCameraPositionState()
    var mapLoaded by remember { mutableStateOf(false) }
    // Resolved here (not inside the marker) so the pin bitmap re-renders once the image is ready.
    val pin = rememberKarikaPinPainter()

    // newLatLngBounds needs a laid-out map, so wait for onMapLoaded before fitting the route.
    LaunchedEffect(latLngs, mapLoaded, showRoute) {
        if (!mapLoaded || latLngs.isEmpty()) return@LaunchedEffect
        val bounds = LatLngBounds.builder().apply { latLngs.forEach(::include) }.build()
        val update = if (!showRoute || bounds.northeast == bounds.southwest) {
            CameraUpdateFactory.newLatLngZoom(latLngs.last(), 15f)
        } else {
            CameraUpdateFactory.newLatLngBounds(bounds, 120)
        }
        cameraState.animate(update)
    }

    LaunchedEffect(focus, mapLoaded) {
        if (!mapLoaded || focus == null) return@LaunchedEffect
        cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(focus.latitude, focus.longitude), 16f))
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true }
    ) {
        if (showRoute && latLngs.size > 1) {
            Polyline(points = latLngs, color = KarikaColors.Primary, width = 10f)
        }
        if (showRoute && latLngs.size > 1) {
            val start = latLngs.first()
            MarkerComposable(
                pin, KarikaColors.Green5,
                state = remember(start) { MarkerState(position = start) },
                title = "Početak",
            ) {
                KarikaPin(pin = pin, color = KarikaColors.Green5, height = 40.dp)
            }
        }
        latLngs.lastOrNull()?.let { last ->
            MarkerComposable(
                pin, KarikaColors.Primary,
                state = remember(last) { MarkerState(position = last) },
                title = "Zadnja lokacija",
                zIndex = 1f,
            ) {
                KarikaPin(pin = pin, color = KarikaColors.Primary, height = 52.dp)
            }
        }
        focus?.let {
            val position = LatLng(it.latitude, it.longitude)
            MarkerComposable(
                pin, KarikaColors.Blue,
                state = remember(position) { MarkerState(position = position) },
                title = "Odabrana tačka",
                zIndex = 2f,
            ) {
                KarikaPin(pin = pin, color = KarikaColors.Blue, height = 46.dp)
            }
        }
    }
}

/** Phone pin from `karika_pin`, outlined in [color], with a tail whose tip is the point (default anchor 0.5,1). */
@Composable
private fun KarikaPin(pin: Painter, color: Color, height: Dp) {
    val width = height * KARIKA_PIN_ASPECT
    val shape = RoundedCornerShape(width * KARIKA_PIN_CORNER)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier
                .size(width = width, height = height)
                .clip(shape)
                .border(2.dp, color, shape),
            painter = pin,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        Canvas(modifier = Modifier.size(width = width * 0.5f, height = width * 0.35f)) {
            drawPath(
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2, size.height)
                    close()
                },
                color
            )
        }
    }
}
