package karika.distribucija.ba.ui.view.main.profile.order.comments

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState

@Composable
fun CommentsView(component: CommentsComponent) {
    val comments = component.comments.collectAsState()
    val state = rememberLazyListState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Komentari") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = state
            ) {
                items(items = comments.value) { item ->
                    CommentItem(item, component)
                }
            }
            EnterComment(component)
        }
    }

    LaunchedEffect(state.canScrollForward) {
        state.scrollToItem(comments.value.size)
    }
}

@Composable
private fun EnterComment(component: CommentsComponent) {
    val comment = component.newComment.asState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaTextField2(
            modifier = Modifier
                .weight(1f),
            value = comment,
            placeholder = "Napiši komentar",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )
        PrimaryButtonFilled(
            modifier = Modifier
                .height(50.dp),
            title = "Pošalji",
            enabled = comment.value.isNotEmpty()
        ) {
            keyboardController?.hide()
            component.sendComment()
        }
    }
}

@Composable
fun CommentItem(comment: Comment, component: CommentsComponent) {
    if (comment.isMine()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .background(
                        color = KarikaColors.MineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.End
            ) {
                if (comment.message == null) {
                    KarikaText(
                        modifier = Modifier
                            .padding(16.dp),
                        text = comment.files?.firstOrNull()?.name,
                        color = KarikaColors.Primary,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                } else {
                    KarikaText(
                        modifier = Modifier
                            .padding(16.dp),
                        text = comment.message,
                        color = KarikaColors.White,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .background(
                        color = KarikaColors.NotMineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                if (comment.message == null) {
                    KarikaText(
                        modifier = Modifier
                            .padding(16.dp),
                        text = comment.files?.firstOrNull()?.name,
                        color = KarikaColors.Primary,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                } else {
                    KarikaText(
                        modifier = Modifier
                            .padding(16.dp),
                        text = comment.message,
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    }
}
