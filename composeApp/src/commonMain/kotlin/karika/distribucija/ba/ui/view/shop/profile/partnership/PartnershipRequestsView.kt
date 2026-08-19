package karika.distribucija.ba.ui.view.shop.profile.partnership

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.domain.model.PartnershipRequest
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.components.roundedWithBorder
import karika.distribucija.ba.ui.view.shop.profile.account.ConfirmationModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_cancel_circle
import karikav2.composeapp.generated.resources.ic_check_circle_filled

@Composable
fun PartnershipRequestsView(component: PartnershipRequestsComponent) {
    val requests by component.requests.collectAsState()
    val error by component.error.collectAsState()
    val approveRequest by component.approveRequest.asState()
    val rejectRequest by component.rejectRequest.asState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Zahtjevi za partnerstvo") {
                component.appBack()
            }
        },
        component = component
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            when {
                error != null -> ErrorState(message = error, onRetry = component::load)
                requests.isEmpty() -> EmptyState()
                else -> RequestList(requests = requests, component = component)
            }
        }

        if (approveRequest != null) {
            ConfirmationModal(
                title = "Prihvati zahtjev",
                message = "Da li ste sigurni da želite prihvatiti zahtjev za partnerstvo od \"${approveRequest?.displayVendorName()}\"?",
                primaryButtonText = "Prihvati",
                secondaryButtonText = "Odustani",
                onPrimaryClick = {
                    approveRequest?.let { component.approve(it) }
                    component.approveRequest.value = null
                },
                onSecondaryClick = {
                    component.approveRequest.value = null
                }
            )
        }

        if (rejectRequest != null) {
            RejectRequestDialog(
                request = rejectRequest!!,
                onReject = { reason ->
                    component.reject(rejectRequest!!, reason)
                    component.rejectRequest.value = null
                },
                onCancel = {
                    component.rejectRequest.value = null
                }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600,
            text = "Trenutno nemate zahtjeva za partnerstvo."
        )
    }
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Primary,
            textSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W700,
            text = message ?: "Došlo je do greške. Pokušajte ponovo!"
        )
        YSpacer16()
        PrimaryButton(
            modifier = Modifier
                .height(48.dp),
            title = "Pokušaj ponovo",
            color = KarikaColors.Primary
        ) {
            onRetry()
        }
    }
}

@Composable
private fun RequestList(
    requests: List<PartnershipRequest>,
    component: PartnershipRequestsComponent
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = requests, key = { it.partnershipId ?: it.hashCode() }) { request ->
            RequestItem(request = request, component = component)
        }
    }
}

@Composable
private fun RequestItem(request: PartnershipRequest, component: PartnershipRequestsComponent) {
    Column(
        modifier = Modifier
            .roundedWithBorder(
                color = KarikaColors.Gray14,
                borderColor = KarikaColors.Border,
                shape = 4.dp
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .rounded(color = KarikaColors.White, shape = 4.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = request.displayVendorName(),
                color = KarikaColors.Black,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            request.requestedAt()?.let { requestedAt ->
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KarikaText(
                        modifier = Modifier,
                        text = "Zahtjev poslat: $requestedAt",
                        color = KarikaColors.Gray2,
                        textSize = 12.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            }
            request.note?.takeIf { it.isNotBlank() }?.let { note ->
                YSpacer8()
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = note,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            YSpacer8()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f),
                    title = "Odbij",
                    color = KarikaColors.Red,
                    icon = Res.drawable.ic_cancel_circle
                ) {
                    component.rejectRequest.value = request
                }
                PrimaryButton(
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f),
                    title = "Prihvati",
                    color = KarikaColors.Green3,
                    icon = Res.drawable.ic_check_circle_filled
                ) {
                    component.approveRequest.value = request
                }
            }
        }
    }
}

@Composable
private fun RejectRequestDialog(
    request: PartnershipRequest,
    onReject: (String?) -> Unit,
    onCancel: () -> Unit
) {
    val reason = mutableStateOf("").asState()

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .rounded(shape = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = "Odbij zahtjev",
                    color = KarikaColors.Gray2,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    text = "Da li ste sigurni da želite odbiti zahtjev za partnerstvo od \"${request.displayVendorName()}\"?",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W600
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Razlog (opcionalno)",
                    value = reason,
                    placeholder = "Unesite razlog odbijanja"
                )
                HorizontalButtons(
                    modifier = Modifier,
                    primaryTitle = "Odbij",
                    secondaryTitle = "Odustani"
                ) {
                    if (it == "Odustani") {
                        onCancel()
                        return@HorizontalButtons
                    }
                    onReject(reason.value.takeIf { it.isNotBlank() })
                }
            }
        }
    }
}
