package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W300
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.jetsetting.core.JetSettingTile
import com.funny.jetsetting.core.ui.SettingItemCategory
import com.funny.translation.helper.toastOnUi
import com.funny.translation.theme.isLight
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.WebViewActivity
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.ui.touchToScale

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    SettingItemCategory(
        title = {
            ItemHeading(text = stringResource(id = R.string.about))
        }
    ) {
        JetSettingTile(
            resourceId = R.drawable.ic_qq,
            text = stringResource(R.string.join_qq_group)
        ) {
            WebViewActivity.start(context, "https://jq.qq.com/?_wv=1027&k=3Bvvfzdu")
        }
        JetSettingTile(
            resourceId = R.drawable.ic_github,
            text = stringResource(R.string.source_code)
        ) {
            context.toastOnUi(FunnyApplication.resources.getText(R.string.welcome_star))
            WebViewActivity.start(context, "https://github.com/FunnySaltyFish/FunnyTranslation")
        }
        JetSettingTile(
            resourceId = R.drawable.ic_open_source_library,
            text = stringResource(id = R.string.open_source_library)
        ) {
            navController.navigate(TranslateScreen.OpenSourceLibScreen.route)
        }
        JetSettingTile(
            resourceId = R.drawable.ic_privacy,
            text = stringResource(R.string.privacy)
        ) {
            WebViewActivity.start(
                context,
                "https://api.funnysaltyfish.fun/trans/v1/api/privacy"
            )
        }
    }
}

@Composable
fun OpenSourceLibScreen() {
    val vm : SettingsScreenViewModel = viewModel()
    val (state, retry) = rememberRetryableLoadingState(loader = vm::loadOpenSourceLibInfo)
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        loadingList(state, retry, key = { it.name }){ info ->
            val color = if (info.author == "FunnySaltyFish" && MaterialTheme.colorScheme.isLight) MaterialColors.Orange200 else MaterialTheme.colorScheme.primaryContainer
            OpenSourceLibItem(
                modifier = Modifier
                    .touchToScale()
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                info = info
            )
        }
    }

}

@Composable
fun OpenSourceLibItem(
    modifier: Modifier = Modifier,
    info: OpenSourceLibraryInfo
) {
    val context = LocalContext.current
    Column(modifier = modifier){
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = info.name, fontWeight = W700)
            Text(text = info.author, fontWeight = W300, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(6f), text = info.description, fontWeight = W500, fontSize = 14.sp)
            IconButton(modifier = Modifier
                .size(48.dp)
                .weight(1f), onClick = {
                WebViewActivity.start(context, info.url)
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "打开网页")
            }
        }
    }
}

//@Composable
//@Preview
//fun OpenSourceLibItemPreview() {
//    OpenSourceLibItem(modifier = Modifier
//        .fillMaxWidth()
//        .wrapContentHeight(), info = OpenSourceLibraryInfo(
//        name="ComposeDataSaver",
//        url= "https://github.com/FunnySaltyFish/ComposeDataSaver",
//        description= "在 Jetpack Compose 中优雅完成数据持久化",
//        author= "FunnySaltyFish"
//    ))
//}