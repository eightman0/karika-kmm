package karika.distribucija.ba.ui.view.distributer.employees.locations

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.EmployeesRepository
import karika.distribucija.ba.domain.model.EmployeeLocationHistory
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorEmployee
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Same presets as the vendor web modal; all stay within the 31-day client limit (API allows 90). */
enum class LocationPeriod(val label: String, val daysBack: Int, val endDaysBack: Int = 0) {
    Today("Danas", 0),
    Yesterday("Jučer", 1, 1),
    Last7("Zadnjih 7 dana", 6),
    Last30("Zadnjih 30 dana", 29),
}

/** `GET /V1/vendor-operations/employees/{employeeId}/locations` for one roster row with `can_view`. */
class EmployeeLocationsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val employee: VendorEmployee
) : CommonComponent(componentContext, stateHolder) {

    private val repository = EmployeesRepository()

    private val _period = MutableStateFlow(LocationPeriod.Today)
    val period = _period.asStateFlow()

    private val _history = MutableStateFlow<EmployeeLocationHistory?>(null)
    val history = _history.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun selectPeriod(period: LocationPeriod) {
        if (_period.value == period && _history.value != null) return
        _period.value = period
        load()
    }

    @OptIn(ExperimentalTime::class)
    fun load() {
        val employeeId = employee.employeeId ?: return
        if (employee.canView != true) return

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val period = _period.value
        val dateFrom: LocalDate = today.minus(period.daysBack, DateTimeUnit.DAY)
        val dateTo: LocalDate = today.minus(period.endDaysBack, DateTimeUnit.DAY)

        scope.launch {
            repository.getLocations(employeeId, dateFrom.toString(), dateTo.toString())
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            // A slower response for a previously selected period must not win.
                            if (_period.value != period) return@collect
                            _error.value = null
                            _history.value = result.data
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            if (_period.value != period) return@collect
                            _history.value = null
                            _error.value = result.message
                        }
                    }
                }
        }
    }
}
