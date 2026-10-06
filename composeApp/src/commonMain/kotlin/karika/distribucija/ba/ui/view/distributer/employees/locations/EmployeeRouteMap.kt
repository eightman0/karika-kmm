package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import karika.distribucija.ba.domain.model.EmployeeLocationPoint

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
