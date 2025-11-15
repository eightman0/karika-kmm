package karika.distribucija.ba.ui.view.main.menu.faq

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import org.jetbrains.compose.resources.vectorResource

@Composable
fun FaqView(component: FaqComponent) {
    val faq by component.faq.collectAsState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Često postavljena pitanja") {
                component.appBack()
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.White,
                    textAlign = TextAlign.Center,
                    atext = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W400,
                                color = KarikaColors.Gray2,
                                fontSize = 16.sp
                            )
                        ) {
                            append("Sve što trebate znati o Karika platormi. Ako ne možete pronaći odgovor koji ste tražili, molimo Vas da nas kontaktirate na ")
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
                                    fontWeight = FontWeight.W600,
                                    color = KarikaColors.Gray2,
                                    fontSize = 16.sp
                                )
                            ) {
                                append("info@karika.ba")
                            }
                        }
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W400,
                                color = KarikaColors.Gray2,
                                fontSize = 16.sp
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
                                    fontWeight = FontWeight.W600,
                                    color = KarikaColors.Gray2,
                                    fontSize = 16.sp
                                )
                            ) {
                                append("033/246-830")
                            }
                        }
                    },
                    textSize = 14.sp,
                    fontWeight = FontWeight.W500
                )
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

    Column(
        modifier = Modifier
            .border(width = 1.dp, color = KarikaColors.Primary, shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .onClick {
                    expanded.negate()
                }
                .background(color = KarikaColors.Primary, shape = RoundedCornerShape(4.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .padding(16.dp)
                    .weight(1f),
                color = KarikaColors.White,
                text = faq.section,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            Icon(
                modifier = Modifier
                    .padding(end = 16.dp),
                imageVector = vectorResource(if (!expanded.value) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_up),
                contentDescription = "",
                tint = KarikaColors.White
            )
        }

        if (expanded.value) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                faq.items?.forEach {
                    val expanded1 = mutableStateOf(false).asState()
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .border(
                                width = 1.dp,
                                color = KarikaColors.Gray20,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .onClick {
                                    expanded1.negate()
                                }
                                .background(
                                    color = KarikaColors.Gray5,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KarikaText(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .padding(8.dp)
                                    .weight(1f),
                                color = KarikaColors.Primary,
                                text = it.question,
                                textSize = 14.sp,
                                fontWeight = FontWeight.W600
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(end = 8.dp),
                                imageVector = vectorResource(if (!expanded1.value) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_up),
                                contentDescription = "",
                                tint = KarikaColors.Primary
                            )
                        }
                        if (expanded1.value) {
                            HtmlTextWithStyles(
                                modifier = Modifier
                                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                textColor = KarikaColors.Gray2,
                                html = it.answer ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}