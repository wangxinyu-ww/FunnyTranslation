package com.funny.trans.login.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.ui.animatedGradientBackground
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

sealed class LoginRoute(val route: String) {
    object LoginPage: LoginRoute("login_page")
    object ResetPasswordPage: LoginRoute("reset_password")
    object FindUsernamePage: LoginRoute("find_user_name")
    object ChangeUsernamePage: LoginRoute("change_user_name")
    object CancelAccountPage: LoginRoute("cancel_account")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginNavigation(
    onLoginSuccess: (UserInfoBean) -> Unit,
) {
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    AnimatedNavHost(
        navController = navController,
        startDestination = LoginRoute.LoginPage.route,
        modifier = Modifier
            .fillMaxSize()
            .animatedGradientBackground(
                 MaterialTheme.colorScheme.surface,
                 MaterialTheme.colorScheme.tertiaryContainer,
            )
            .statusBarsPadding(),
    ){
        addLoginRoutes(navController, onLoginSuccess = onLoginSuccess)
    }
}

fun NavGraphBuilder.addLoginRoutes(
    navController: NavHostController,
    onLoginSuccess: (UserInfoBean) -> Unit,
){
    animateComposable(LoginRoute.LoginPage.route){
        LoginPage(navController = navController, onLoginSuccess = onLoginSuccess)
    }
    animateComposable(LoginRoute.ResetPasswordPage.route){
        ResetPasswordPage(navController = navController)
    }
    animateComposable(LoginRoute.FindUsernamePage.route){
        FindUsernamePage()
    }
    animateComposable(LoginRoute.ChangeUsernamePage.route){
        ChangeUsernamePage(navController = navController)
    }
    animateComposable(LoginRoute.CancelAccountPage.route){
        CancelAccountPage(navController = navController)
    }
}


@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.animateComposable(
    route: String,
    animDuration: Int = 700,
    content: @Composable () -> Unit,
) {
    composable(
        route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = tween(animDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = tween(animDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Down,
                animationSpec = tween(animDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Down,
                animationSpec = tween(animDuration)
            )
        }
    ) {
        content()
    }
}