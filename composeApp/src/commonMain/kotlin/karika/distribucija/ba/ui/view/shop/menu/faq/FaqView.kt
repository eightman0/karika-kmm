package karika.distribucija.ba.ui.view.shop.menu.faq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import org.jetbrains.compose.resources.vectorResource

@Composable
fun FaqView(component: FaqComponent) {
    val faq by component.faq.collectAsState()

    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(title = "Često postavljena pitanja", onBack = { component.appBack() })
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                KCard(modifier = Modifier.fillMaxWidth()) {
                    KarikaText(
                        modifier = Modifier.padding(16.dp),
                        color = KarikaUiColors.Muted,
                        atext = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W400,
                                    color = KarikaUiColors.Muted,
                                    fontSize = 14.sp
                                )
                            ) {
                                append("Sve što trebate znati o Karika platformi. Ako ne možete pronaći odgovor koji ste tražili, molimo Vas da nas kontaktirate na ")
                            }
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "",
                                    styles = TextLinkStyles(),
                                    linkInteractionListener = {
                                        openEmail("info@karika.ba")
                                    }
                                )
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.W700,
                                        color = KarikaUiColors.Pink,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    append("info@karika.ba")
                                }
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W400,
                                    color = KarikaUiColors.Muted,
                                    fontSize = 14.sp
                                )
                            ) {
                                append(" ili ")
                            }
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "",
                                    styles = TextLinkStyles(),
                                    linkInteractionListener = {
                                        openPhoneCall("033246830")
                                    }
                                )
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.W700,
                                        color = KarikaUiColors.Pink,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    append("033/246-830")
                                }
                            }
                        },
                        textSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            }
            items(items = faq) { item ->
                FaqItem(item, component)
            }
        }
    }
}

@Composable
private fun FaqItem(faq: Faq, component: FaqComponent) {
    val expanded = mutableStateOf(false).asState()

    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded.negate()
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                color = KarikaUiColors.Ink,
                text = faq.section,
                textSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W700
            )
            Spacer(Modifier.width(12.dp))
            KIcon(
                modifier = Modifier.rotate(if (expanded.value) 180f else 0f),
                icon = vectorResource(Res.drawable.ic_k_chevron_down),
                tint = KarikaUiColors.Pink,
                size = 20.dp
            )
        }

        if (expanded.value) {
            faq.items?.forEach {
                val expanded1 = mutableStateOf(false).asState()
                KDivider()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded1.negate()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier.weight(1f),
                            color = if (expanded1.value) KarikaUiColors.Pink else KarikaUiColors.Ink,
                            text = it.question,
                            textSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.width(12.dp))
                        KIcon(
                            modifier = Modifier.rotate(if (expanded1.value) 180f else 0f),
                            icon = vectorResource(Res.drawable.ic_k_chevron_down),
                            tint = KarikaUiColors.Subtle,
                            size = 18.dp
                        )
                    }
                    if (expanded1.value) {
                        HtmlTextWithStyles(
                            modifier = Modifier
                                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                            textColor = KarikaUiColors.Muted,
                            html = it.answer ?: ""
                        )
                    }
                }
            }
        }
    }
}
