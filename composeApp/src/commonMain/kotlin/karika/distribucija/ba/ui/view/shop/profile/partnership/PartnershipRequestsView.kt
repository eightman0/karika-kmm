package karika.distribucija.ba.ui.view.shop.profile.partnership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.shop.profile.account.ConfirmationModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PartnershipRequestsView(component: PartnershipRequestsComponent) {
    val requests by component.requests.collectAsState()
    val error by component.error.collectAsState()
    val approveRequest by component.approveRequest.asState()
    val rejectRequest by component.rejectRequest.asState()

    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(title = "Zahtjevi za partnerstvo", onBack = { component.appBack() })
            }
        },
        component = component
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(KarikaUiColors.Page)
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
        KEmptyState(text = "Trenutno nemate zahtjeva za partnerstvo.")
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
            color = KarikaUiColors.Ink,
            textSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600,
            text = message ?: "Došlo je do greške. Pokušajte ponovo!"
        )
        Spacer(Modifier.height(16.dp))
        KPrimaryButton(
            text = "Pokušaj ponovo",
            height = 48.dp
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
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = requests, key = { it.partnershipId ?: it.hashCode() }) { request ->
            RequestItem(request = request, component = component)
        }
        item {
            Column {
                Spacer(Modifier.height(20.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun RequestItem(request: PartnershipRequest, component: PartnershipRequestsComponent) {
    KCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KInitials(
                    name = request.displayVendorName(),
                    size = 44.dp,
                    shape = CircleShape,
                    textSize = 15.sp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    KarikaText(
                        text = request.displayVendorName(),
                        color = KarikaUiColors.Ink,
                        textSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.W700
                    )
                    request.requestedAt()?.let { requestedAt ->
                        Spacer(Modifier.height(3.dp))
                        KarikaText(
                            text = "Zahtjev poslat: $requestedAt",
                            color = KarikaUiColors.Muted,
                            textSize = 12.sp
                        )
                    }
                }
            }
            request.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaUiColors.Field, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    KarikaText(
                        text = note,
                        color = KarikaUiColors.Ink,
                        textSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KSecondaryButton(
                    modifier = Modifier.weight(1f),
                    text = "Odbij",
                    height = 44.dp,
                    textColor = KarikaUiColors.Red
                ) {
                    component.rejectRequest.value = request
                }
                KPrimaryButton(
                    modifier = Modifier.weight(1f),
                    text = "Prihvati",
                    height = 44.dp,
                    background = KarikaUiColors.Green,
                    icon = vectorResource(Res.drawable.ic_k_check)
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
                    color = KarikaUiColors.Ink,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    text = "Da li ste sigurni da želite odbiti zahtjev za partnerstvo od \"${request.displayVendorName()}\"?",
                    color = KarikaUiColors.Muted,
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
