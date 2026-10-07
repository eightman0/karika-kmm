package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.karika_pin
import org.jetbrains.compose.resources.imageResource

/** Native map with the employee's route: Google Maps on Android, MapKit on iOS.
 * [points] are chronological; [focus] is a timeline point the camera should move to.
 * Only the latest point is pinned unless [showRoute] is on, which adds the full trace and start pin. */
@Composable
expect fun EmployeeRouteMap(
    points: List<EmployeeLocationPoint>,
    focus: EmployeeLocationPoint?,
    showRoute: Boolean,
    modifier: Modifier = Modifier
)

/** `karika_pin.jpg` is a phone mockup on a white, non-transparent background, so pins draw only
 * the phone body (this source rect) and clip it to the phone's rounded corners. */
private val KarikaPinSrcOffset = IntOffset(18, 33)
private val KarikaPinSrcSize = IntSize(828, 1724)

/** Width / height of the cropped phone. */
internal const val KARIKA_PIN_ASPECT = 828f / 1724f

/** Corner radius as a fraction of the pin width. */
internal const val KARIKA_PIN_CORNER = 0.16f

@Composable
internal fun rememberKarikaPinPainter(): Painter {
    val image = imageResource(Res.drawable.karika_pin)
    return remember(image) { BitmapPainter(image, KarikaPinSrcOffset, KarikaPinSrcSize) }
}
