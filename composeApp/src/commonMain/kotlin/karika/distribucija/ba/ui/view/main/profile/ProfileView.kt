package karika.distribucija.ba.ui.view.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.YSpacer16
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_logout
import karikav2.composeapp.generated.resources.ic_navigation_profile
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProfileView(viewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.Background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(viewModel)
            Actions(viewModel)
        }
    }
}

@Composable
private fun Header(viewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = KarikaColors.Red1, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_navigation_profile),
                    contentDescription = ""
                )
            }
            KarikaText(
                modifier = Modifier
                    .clickable {

                    },
                color = KarikaColors.Black,
                fontWeight = FontWeight.W700,
                textSize = 18.sp,
                text = "Karika d.o.o"
            )
        }
    }

}

@Composable
private fun Actions(viewModel: ProfileViewModel) {
    Column(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryButton(
                modifier = Modifier
                    .weight(1f),
                title = "Moj nalog",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2
            ) {

            }
            PrimaryButton(
                modifier = Modifier
                    .weight(1f),
                title = "Moje narudžbe",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2
            ) {

            }
        }
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryButton(
                modifier = Modifier
                    .weight(1f),
                title = "Poruke admina",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2
            ) {

            }
            PrimaryButton(
                modifier = Modifier
                    .weight(1f),
                title = "Poruke dobavljača",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2
            ) {

            }
        }
        YSpacer16()
        PrimaryButton(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            title = "Moji bodovi",
            icon = Res.drawable.ic_navigation_profile,
            color = KarikaColors.Gray2
        ) {

        }
        YSpacer16()
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        PrimaryButton(
            modifier = Modifier,
            title = "Odjava",
            icon = Res.drawable.ic_logout,
            color = KarikaColors.Primary,
            textSize = 16.sp,
        ) {

        }
    }
}