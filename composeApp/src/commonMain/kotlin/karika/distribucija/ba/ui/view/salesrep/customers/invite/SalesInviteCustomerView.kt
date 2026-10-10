package karika.distribucija.ba.ui.view.salesrep.customers.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_info
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_search
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesInviteCustomerView(component: SalesInviteCustomerComponent) {
    val email by component.email.collectAsState()
    val note by component.note.collectAsState()
    val contactMethod by component.contactMethod.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val searchQuery by component.searchQuery.collectAsState()
    val searchResults by component.searchResults.collectAsState()
    val isSearching by component.isSearching.collectAsState()
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ── Info banner ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(VendorAccentSoft)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_info), tint = VendorAccent, size = 18.dp)
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Kupac dobija zahtjev za partnerstvo i mora ga prihvatiti da bi se pojavio na vašoj listi.",
                color = KarikaUiColors.Ink,
                textSize = 13.sp,
                lineHeight = 19.sp
            )
        }

        // ── Kupac ──────────────────────────────────────────────────────────────
        KSectionTitle(modifier = Modifier.padding(top = 22.dp, bottom = 12.dp), title = "Kupac")
        KCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    KFieldLabel(text = "Pretraži kupca", required = true, requiredColor = VendorAccent)
                    var searchExpanded by remember { mutableStateOf(false) }
                    val shape = RoundedCornerShape(12.dp)
                    ExposedDropdownMenuBox(
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it }
                    ) {
                        Row(
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(shape)
                                .background(KarikaColors.White)
                                .border(1.dp, if (searchExpanded) VendorAccent else KarikaUiColors.Border, shape)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KIcon(icon = vectorResource(Res.drawable.ic_k_search), tint = KarikaUiColors.Muted, size = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { component.setSearchQuery(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = KarikaUiColors.Ink,
                                    fontSize = 15.sp,
                                    fontFamily = karikaFonts()
                                ),
                                cursorBrush = SolidColor(VendorAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { if (it.isFocused) searchExpanded = true },
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            KarikaText(
                                                text = "Ime, kompanija ili email",
                                                color = KarikaUiColors.Subtle,
                                                textSize = 15.sp,
                                                maxLines = 1
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                            if (isSearching) {
                                Spacer(Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    color = VendorAccent,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(KarikaUiColors.Field)
                                        .clickable {
                                            component.setSearchQuery("")
                                            searchExpanded = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    KIcon(icon = vectorResource(Res.drawable.ic_k_close), tint = KarikaUiColors.Muted, size = 14.dp)
                                }
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = searchExpanded,
                            containerColor = KarikaColors.White,
                            shape = RoundedCornerShape(12.dp),
                            onDismissRequest = { searchExpanded = false }
                        ) {
                            if (searchQuery.length < 3) {
                                MenuHint("Pretraži kupce (unesite najmanje 3 znaka)")
                            } else if (searchResults.isEmpty()) {
                                MenuHint("Nema korisnika za taj pretragu")
                            } else {
                                searchResults.forEachIndexed { index, customer ->
                                    if (index > 0) KDivider()
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                KarikaText(
                                                    text = customer.company ?: customer.fullName,
                                                    color = KarikaUiColors.Ink,
                                                    textSize = 15.sp,
                                                    fontWeight = FontWeight.W600
                                                )
                                                if (!customer.email.isNullOrBlank()) {
                                                    KarikaText(
                                                        text = customer.email,
                                                        color = KarikaUiColors.Muted,
                                                        textSize = 12.5.sp
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            component.selectCustomer(customer)
                                            searchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // The email the request goes to (chosen customer's, or the one from "Novi kupac")
                if (email.isNotBlank()) {
                    Column {
                        KFieldLabel(text = "Email kupca")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KarikaUiColors.Field)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KIcon(icon = vectorResource(Res.drawable.ic_k_mail), tint = KarikaUiColors.Muted, size = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            KarikaText(
                                modifier = Modifier.weight(1f),
                                text = email,
                                color = KarikaUiColors.Ink,
                                textSize = 15.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // The phone/Viber contact method picker of the component is not offered (it was turned off before)

        // ── Napomena ───────────────────────────────────────────────────────────
        KSectionTitle(modifier = Modifier.padding(top = 22.dp, bottom = 12.dp), title = "Napomena (opcionalno)")
        KCard(modifier = Modifier.fillMaxWidth()) {
            val shape = RoundedCornerShape(12.dp)
            BasicTextField(
                value = note,
                onValueChange = { component.setNote(it) },
                textStyle = TextStyle(
                    color = KarikaUiColors.Ink,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(VendorAccent),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(shape)
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaUiColors.Border, shape)
                    .padding(14.dp),
                decorationBox = { inner ->
                    Box {
                        if (note.isEmpty()) {
                            KarikaText(
                                text = "Kratka napomena za kupca",
                                color = KarikaUiColors.Subtle,
                                textSize = 15.sp
                            )
                        }
                        inner()
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KSecondaryButton(
                modifier = Modifier.weight(1f),
                text = "Odustani",
                onClick = { component.goBack() }
            )
            KPrimaryButton(
                modifier = Modifier.weight(1.6f),
                text = if (contactMethod == ContactMethod.EMAIL) "Pošalji zahtjev" else "Pozovi",
                background = VendorAccent,
                enabled = !isSaving,
                onClick = {
                    if (contactMethod == ContactMethod.EMAIL) {
                        component.send()
                    } else {
                        component.callTarget()?.let { uriHandler.openUri(it) }
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MenuHint(text: String) {
    DropdownMenuItem(
        text = {
            KarikaText(text = text, color = KarikaUiColors.Muted, textSize = 14.sp)
        },
        enabled = false,
        onClick = {}
    )
}
