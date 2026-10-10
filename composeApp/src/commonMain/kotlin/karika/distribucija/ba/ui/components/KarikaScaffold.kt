package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.common.CommonComponent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_info
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource


/** Test tag of the snackbar's message, which a tap dismisses, for the end-to-end tests. */
const val SNACKBAR_TAG = "snackbar"

@Composable
fun KarikaScaffold(
    modifier: Modifier = Modifier,
    component: CommonComponent,
    containerColor: Color = KarikaColors.Primary,
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    disableSnackBar: Boolean = true,
    ignoreImeTweak: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val imeVisible = rememberImeVisible()

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = KarikaColors.Primary,
        snackbarHost = {
            Box(
                modifier = Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (!disableSnackBar) {
                    SnackbarHost(
                        modifier = Modifier
                            .padding(bottom = 100.dp),
                        hostState = component.snackbarHostState
                    ) { data ->
                        KSnackbar(
                            message = data.visuals.message,
                            type = data.visuals.actionLabel,
                            onDismiss = { data.dismiss() }
                        )

                        LaunchedEffect(data) {
                            delay(1500)
                            data.dismiss()
                        }
                    }
                }
            }
        },
        topBar = topBar,
        bottomBar = {
            if (ignoreImeTweak) {
                bottomBar.invoke()
                return@Scaffold
            }

            if (!imeVisible) {
                bottomBar.invoke()
            }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = contentWindowInsets
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)

        ) {
            content.invoke(PaddingValues(0.dp))
        }
    }
}

@Composable
fun rememberImeVisible(): Boolean {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(imeInsets, density) {
        snapshotFlow { imeInsets.getBottom(density) }
            .collect { bottomPixels ->
                isVisible = bottomPixels > 0
            }
    }

    return isVisible
}

/** Snackbar in the redesign style: white card, tinted icon per message type and the message. */
@Composable
private fun KSnackbar(
    message: String,
    type: String?,
    onDismiss: () -> Unit
) {
    val style = type.toSnackbarStyle()
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = KarikaUiColors.Ink, spotColor = KarikaUiColors.Ink)
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaUiColors.Line, RoundedCornerShape(16.dp))
            .testTag(SNACKBAR_TAG)
            .onClick { onDismiss() }
            .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(style.soft),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(style.icon), tint = style.color, size = 18.dp)
        }
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = message,
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.width(10.dp))
        KIcon(
            icon = vectorResource(Res.drawable.ic_k_close),
            tint = KarikaUiColors.Subtle,
            size = 16.dp
        )
    }
}

private class SnackbarStyle(val color: Color, val soft: Color, val icon: DrawableResource)

private fun String?.toSnackbarStyle(): SnackbarStyle {
    return when (this) {
        "ERROR" -> SnackbarStyle(KarikaUiColors.Red, KarikaUiColors.RedSoft, Res.drawable.ic_k_close)
        "WARNING" -> SnackbarStyle(KarikaUiColors.Amber, KarikaUiColors.AmberSoft, Res.drawable.ic_k_info)
        else -> SnackbarStyle(KarikaUiColors.Green, KarikaUiColors.GreenSoft, Res.drawable.ic_k_check)
    }
}
