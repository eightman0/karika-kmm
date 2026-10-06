package karika.distribucija.ba.ui.view.distributer.employees

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorEmployee
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.XSpacer8
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_location
import karikav2.composeapp.generated.resources.ic_person
import karikav2.composeapp.generated.resources.ic_phone
import org.jetbrains.compose.resources.vectorResource

@Composable
fun EmployeesView(component: EmployeesComponent) {
    val employees by component.employees.collectAsState()
    val totalCount by component.totalCount.collectAsState()
    val statusFilter by component.statusFilter.collectAsState()
    val loaded by component.loaded.collectAsState()
    val loader by component.loader.collectAsState()
    val me by component.stateHolder.salesSpecificHandler.me.collectAsState()
    val canView = me.vendorOperationsEnabled && me.capabilities.canViewEmployees
    val state = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KarikaText(
                    text = "Komercijalisti",
                    color = KarikaColors.Gray2,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    text = "Pregled tima i historija lokacija na terenu.",
                    color = KarikaColors.Gray7,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }

        if (!canView) {
            item {
                EmployeesInfoState(
                    title = "Pregled tima nije dostupan",
                    text = if (!me.vendorOperationsEnabled) {
                        "Vendor Operations nije uključen za ovog distributera."
                    } else {
                        "Vaša uloga nema pravo pregleda komercijalista."
                    }
                )
            }
            return@LazyColumn
        }

        item {
            SearchBoxBorder(
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Pretraži po imenu ili emailu",
                onValueChange = { if (it.isBlank()) component.search("") },
                onClose = { component.search("") },
                onSearchExecute = { component.search(it) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmployeeStatusFilter.entries.forEach { filter ->
                    EmployeesPill(
                        text = filter.label,
                        selected = statusFilter == filter,
                        onClick = { component.setStatusFilter(filter) }
                    )
                }
            }
        }

        if (loaded) {
            item {
                KarikaText(
                    text = "Ukupno: $totalCount",
                    color = KarikaColors.Gray13,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }

        items(items = employees, key = { it.employeeId ?: it.hashCode().toLong() }) {
            EmployeeCard(employee = it, onOpenLocations = { component.openLocations(it) })
        }

        if (loaded && !loader && employees.isEmpty()) {
            item {
                EmployeesInfoState(
                    title = "Nema komercijalista",
                    text = "Nema rezultata za odabrane filtere."
                )
            }
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    // `/me` is fetched when the dashboard opens, so the gate can flip after first composition.
    LaunchedEffect(canView) {
        if (canView) component.loadNextPage(true)
    }
}

@Composable
private fun EmployeeCard(employee: VendorEmployee, onOpenLocations: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeeAvatar(employee, 44.dp)
            XSpacer8()
            Column(
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KarikaText(
                    text = employee.fullName,
                    color = KarikaColors.Gray2,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoleBadge(employee.role)
                    StatusBadge(employee.status)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            employee.email?.takeIf { it.isNotBlank() }?.let { email ->
                ContactRow(
                    icon = vectorResource(Res.drawable.ic_email),
                    text = email,
                    onClick = { runCatching { uriHandler.openUri("mailto:$email") } }
                )
            }
            employee.telephone?.takeIf { it.isNotBlank() }?.let { phone ->
                ContactRow(
                    icon = vectorResource(Res.drawable.ic_phone),
                    text = phone,
                    onClick = { runCatching { uriHandler.openUri("tel:$phone") } }
                )
            }
            employee.managerName?.takeIf { it.isNotBlank() }?.let { manager ->
                ContactRow(
                    icon = vectorResource(Res.drawable.ic_person),
                    text = "Nadređeni: $manager"
                )
            }
        }

        if (employee.canOpenLocations) {
            HorizontalDivider(thickness = 1.dp, color = KarikaColors.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(callback = onOpenLocations)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = vectorResource(Res.drawable.ic_location),
                    contentDescription = null,
                    tint = KarikaColors.Blue
                )
                XSpacer8()
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = "Historija lokacija",
                    color = KarikaColors.Blue,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = vectorResource(Res.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = KarikaColors.Blue
                )
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.onClick(callback = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = icon,
            contentDescription = null,
            tint = KarikaColors.Gray13
        )
        XSpacer8()
        KarikaText(
            text = text,
            color = if (onClick != null) KarikaColors.Gray18 else KarikaColors.Gray13,
            textSize = 13.sp,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
    }
}

@Composable
internal fun EmployeeAvatar(employee: VendorEmployee, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(KarikaColors.Blue3_10),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = employee.initials,
            color = KarikaColors.Blue,
            textSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun RoleBadge(role: String?) {
    EmployeesBadge(text = roleLabel(role), background = KarikaColors.Gray20, textColor = KarikaColors.Gray18)
}

@Composable
internal fun StatusBadge(status: String?) {
    val (label, background, textColor) = when (status) {
        "active" -> Triple("Aktivan", KarikaColors.Green4, KarikaColors.Green3)
        "invited" -> Triple("Pozvan", KarikaColors.Yellow1, KarikaColors.Yellow2)
        "suspended" -> Triple("Suspendovan", KarikaColors.Red2, KarikaColors.Red3)
        "deleted" -> Triple("Obrisan", KarikaColors.Gray20, KarikaColors.Gray13)
        else -> return
    }
    EmployeesBadge(text = label, background = background, textColor = textColor)
}

internal fun roleLabel(role: String?) = when (role) {
    "vendor_admin" -> "Administrator"
    "vendor_manager" -> "Menadžer"
    "sales_employee" -> "Komercijalista"
    else -> "Zaposlenik"
}

@Composable
private fun EmployeesBadge(text: String, background: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        KarikaText(text = text, color = textColor, textSize = 11.sp, fontWeight = FontWeight.W600)
    }
}

@Composable
internal fun EmployeesPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) KarikaColors.Blue else KarikaColors.Gray20)
            .onClick(callback = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        KarikaText(
            text = text,
            color = if (selected) KarikaColors.White else KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
internal fun EmployeesInfoState(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray14)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        KarikaText(
            text = title,
            color = KarikaColors.Gray2,
            textSize = 15.sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center
        )
        KarikaText(
            text = text,
            color = KarikaColors.Gray13,
            textSize = 13.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center
        )
    }
}
