@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.funny.trans.login.R
import com.funny.translation.AppConfig
import com.funny.translation.helper.toastOnUi
import com.funny.translation.ui.MarkdownText

@Composable
fun CancelAccountPage(
    navController: NavHostController
) {
    Column(
        Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val vm = viewModel<LoginViewModel>()
        val context = LocalContext.current

        SideEffect {
            AppConfig.userInfo.value.takeIf { it.isValid() }?.let {
                vm.email = it.email
                vm.username = it.username
            }
        }

        TipDialog(navController)

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputUsername(
                usernameProvider = vm::username,
                updateUsername = vm::updateUsername,
                isValidUsernameProvider = vm::isValidUsername
            )
            Spacer(modifier = Modifier.height(8.dp))
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendCancelAccountEmail(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val enable by remember {
                derivedStateOf {
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.cancelAccount(context, onSuccess = {
                    context.toastOnUi("账号注销成功")
                    AppConfig.logout()
                    navController.popBackStack() // 账号详情
                    navController.popBackStack() // 主页
                })
            }, enabled = enable) {
                Text(text = "确认注销")
            }
        }
    }
}

@Composable
fun TipDialog(navController: NavController) {
    var showTipDialog by remember { mutableStateOf(true) }
    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = { showTipDialog = false },
            title = { Text("警告") },
            text = { MarkdownText("请注意，您正在尝试注销您的账户。一旦注销完成，该账户将无法再使用，并且**无法撤销**。谨慎操作，确保您已备份、转移或保存了与该账户相关的所有重要资料和数据。如果您确认要注销，请继续进行操作。如有任何疑问，请随时联系我们！") },
            confirmButton = {
                CountDownTimeButton(
                    modifier = Modifier,
                    onClick = { showTipDialog = false },
                    text = stringResource(id = R.string.confirm),
                    initialSent = true,
                    countDownTime = 10
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showTipDialog = false
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}